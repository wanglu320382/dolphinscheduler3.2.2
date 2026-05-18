/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.plugin.task.dataxml.handler;

import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.dataxml.core.ParamContext;
import org.apache.dolphinscheduler.plugin.task.dataxml.datasource.DataSourceResolver;
import org.apache.dolphinscheduler.plugin.task.dataxml.util.ConvertUtil;
import org.apache.dolphinscheduler.plugin.task.dataxml.util.DbHelper;
import org.apache.dolphinscheduler.plugin.task.dataxml.util.PlaceholderUtils;
import org.apache.dolphinscheduler.plugin.task.dataxml.util.TaskDetailMessage;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.query.XDataQueryBuilder;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.query.XdataQueryBuilderFactory;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.Column;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.TaskType;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.XdataTemplate;

import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Db2DbHandler {

    private static final int MAX_COMMIT_RECORDS = 1000;
    private static final int QUERY_TIMEOUT = 1800;

    private final DataSourceResolver dataSourceResolver;

    public void execute(XdataTemplate template, ParamContext param) throws TaskException, SQLException {
        if (TaskType.PARA.equals(template.getTaskType())) {
            throw new TaskException("taskType=PARA 请使用 busType=DB2PARA");
        }
        if (TaskType.INSERT.equals(template.getTaskType())) {
            importInsert(template, param);
        } else if (TaskType.UPDATE.equals(template.getTaskType())) {
            importUpdate(template, param);
        } else if (TaskType.SQL.equals(template.getTaskType())) {
            importSql(template, param);
        } else {
            throw new TaskException("DB2DB 未识别的 taskType：" + template.getTaskType());
        }
    }

    private void importSql(XdataTemplate template, ParamContext param) throws TaskException, SQLException {
        Connection toConn = dataSourceResolver.getConnection(template.getToDs());
        String execSql = template.getExecSql();
        try {
            execSql = PlaceholderUtils.replace(execSql, param, false);
            log.info("生成execSql：{}", execSql);
            PreparedStatement pstmt = toConn.prepareStatement(execSql, ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            pstmt.setQueryTimeout(QUERY_TIMEOUT);
            pstmt.execute();
            toConn.commit();
            param.put("totalTables", 1L + param.getLong("totalTables"));
        } catch (Exception e) {
            throw new TaskException(TaskDetailMessage.labeledLines(e, "execSql", execSql));
        } finally {
            DbHelper.closeConnection(toConn);
        }
    }

    private void importUpdate(XdataTemplate template, ParamContext param) throws SQLException, TaskException {
        Connection fromConn = dataSourceResolver.getConnection(template.getFromDs());
        Connection toConn = dataSourceResolver.getConnection(template.getToDs());
        List<Column> columns = template.getColumn();
        XDataQueryBuilder queryBuilder = XdataQueryBuilderFactory.genXdataQueryBuilder(template);
        PreparedStatement updatePstmt = null;
        String selectSql = "";
        String updateSql = "";
        try {
            List<Class<?>> columnClzs = getToColumnClzs(toConn, template);
            int total = 0;
            int commitCount = 0;
            selectSql = queryBuilder.genQuerySQL(template, param);
            log.info("生成selectSql：{}", selectSql);
            updateSql = queryBuilder.genUpdateSQL(template, param);
            log.info("生成updateSql：{}", updateSql);
            PreparedStatement pstmt = fromConn.prepareStatement(selectSql, ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            pstmt.setQueryTimeout(QUERY_TIMEOUT);
            pstmt.setFetchSize(MAX_COMMIT_RECORDS);
            ResultSet rs = pstmt.executeQuery();
            updatePstmt = toConn.prepareStatement(updateSql);
            while (rs.next()) {
                for (int i = 0; i < columns.size(); i++) {
                    updatePstmt.setObject(i + 1, ConvertUtil.convertToObject(rs.getObject(i + 1), columnClzs.get(i)));
                }
                total++;
                commitCount++;
                updatePstmt.addBatch();
                if (commitCount == 1000) {
                    updatePstmt.executeBatch();
                    updatePstmt.clearBatch();
                    toConn.commit();
                    commitCount = 0;
                }
            }
            updatePstmt.executeBatch();
            updatePstmt.clearBatch();
            toConn.commit();
            param.put("totalTables", 1L + param.getLong("totalTables"));
            param.put("totalRecords", total + param.getLong("totalRecords"));
            PlaceholderUtils.appendTableMsg(param, template.getToObjectName(), total);
        } catch (Exception e) {
            String rollbackSql = queryBuilder.genRollBackSQL(template, param);
            if (StringUtils.isNotEmpty(rollbackSql)) {
                DbHelper.executeUpdate(toConn, rollbackSql, null);
                toConn.commit();
            }
            throw new TaskException(
                    TaskDetailMessage.labeledLines(e, "selectSql", selectSql, "updateSql", updateSql));
        } finally {
            DbHelper.closeConnection(fromConn);
            DbHelper.closeConnection(toConn);
        }
    }

    private void importInsert(XdataTemplate template, ParamContext param) throws SQLException, TaskException {
        Connection fromConn = dataSourceResolver.getConnection(template.getFromDs());
        Connection toConn = dataSourceResolver.getConnection(template.getToDs());
        List<Column> columns = template.getColumn();
        XDataQueryBuilder queryBuilder = XdataQueryBuilderFactory.genXdataQueryBuilder(template);
        String selectSql = "";
        String insertSql = "";
        try {
            String deleteSql = queryBuilder.genClearOldDataSQLByCond(template, param);
            List<Class<?>> columnClzs = getToColumnClzs(toConn, template);
            int total = 0;
            int commitCount = 0;
            selectSql = queryBuilder.genQuerySQL(template, param);
            log.info("生成selectSql：{}", selectSql);
            insertSql = queryBuilder.genInsertSQL(template, param);
            log.info("生成insertSql：{}", insertSql);
            PreparedStatement pstmt = fromConn.prepareStatement(selectSql, ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            pstmt.setQueryTimeout(QUERY_TIMEOUT);
            pstmt.setFetchSize(MAX_COMMIT_RECORDS);
            ResultSet rs = pstmt.executeQuery();
            PreparedStatement insertPstmt = toConn.prepareStatement(insertSql);
            while (rs.next()) {
                if (total == 0 && StringUtils.isNotEmpty(deleteSql)) {
                    log.info("执行deleteSql：{}", deleteSql);
                    DbHelper.executeUpdate(toConn, deleteSql, null);
                    toConn.commit();
                }
                for (int i = 0; i < columns.size(); i++) {
                    insertPstmt.setObject(i + 1, ConvertUtil.convertToObject(rs.getObject(i + 1), columnClzs.get(i)));
                }
                total++;
                commitCount++;
                insertPstmt.addBatch();
                if (commitCount == 500) {
                    insertPstmt.executeBatch();
                    insertPstmt.clearBatch();
                    toConn.commit();
                    commitCount = 0;
                }
            }
            insertPstmt.executeBatch();
            insertPstmt.clearBatch();
            toConn.commit();
            param.put("totalTables", 1L + param.getLong("totalTables"));
            param.put("totalRecords", total + param.getLong("totalRecords"));
            PlaceholderUtils.appendTableMsg(param, template.getToObjectName(), total);
        } catch (Exception e) {
            String rollbackSql = queryBuilder.genRollBackSQL(template, param);
            if (StringUtils.isNotEmpty(rollbackSql)) {
                DbHelper.executeUpdate(toConn, rollbackSql, null);
                toConn.commit();
            }
            throw new TaskException(
                    TaskDetailMessage.labeledLines(e, "selectSql", selectSql, "insertSql", insertSql));
        } finally {
            DbHelper.closeConnection(fromConn);
            DbHelper.closeConnection(toConn);
        }
    }

    private List<Class<?>> getToColumnClzs(Connection conn, XdataTemplate template) throws TaskException {
        String toObjectName = template.getToObjectName();
        String toTableClmQuerySql = "select "
                + XDataQueryBuilder.getColumnNames(template, XDataQueryBuilder.COLUMN_TO_TABLE) + " from "
                + toObjectName + " where 1<>1";
        log.info("导入表字段 JAVA 类型查询 SQL：{}", toTableClmQuerySql);
        List<Class<?>> columnClzs = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(toTableClmQuerySql)) {
            ResultSet rs = pstmt.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            for (int i = 0, n = rsmd.getColumnCount(); i < n; i++) {
                columnClzs.add(PlaceholderUtils.resolveClass(rsmd.getColumnClassName(i + 1)));
            }
        } catch (Exception e) {
            throw new TaskException(TaskDetailMessage.labeledLines(e, "toTableClmQuerySql", toTableClmQuerySql));
        }
        return columnClzs;
    }
}
