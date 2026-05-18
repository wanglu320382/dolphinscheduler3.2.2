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
import org.apache.dolphinscheduler.plugin.task.dataxml.util.DbHelper;
import org.apache.dolphinscheduler.plugin.task.dataxml.util.PlaceholderUtils;
import org.apache.dolphinscheduler.plugin.task.dataxml.util.TaskDetailMessage;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.query.XDataQueryBuilder;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.query.XdataQueryBuilderFactory;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.Column;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.XdataTemplate;

import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Db2ParaHandler {

    private static final int QUERY_TIMEOUT = 1800;
    private static final int FETCH_SIZE = 1000;

    private final DataSourceResolver dataSourceResolver;

    public void execute(XdataTemplate template, ParamContext param) throws TaskException, SQLException {
        String dsKey = template.getFromDs();
        if (StringUtils.isEmpty(dsKey)) {
            dsKey = template.getToDs();
        }
        Connection conn = dataSourceResolver.getConnection(dsKey);
        List<Column> columns = template.getColumn();
        if (columns == null || columns.isEmpty()) {
            throw new TaskException("DB2PARA 须配置 column 映射");
        }
        String selectSql = "";
        try {
            if (StringUtils.isNotEmpty(template.getFromObjectName())) {
                String inner = PlaceholderUtils.replace(template.getFromObjectName(), param, false);
                StringBuilder colSel = new StringBuilder();
                for (int i = 0; i < columns.size(); i++) {
                    if (i > 0) {
                        colSel.append(", ");
                    }
                    colSel.append(columns.get(i).getFromName());
                }
                selectSql = "select " + colSel + " from (" + inner + ") t_db2para";
            } else {
                XDataQueryBuilder queryBuilder = XdataQueryBuilderFactory.genXdataQueryBuilder(template);
                selectSql = queryBuilder.genQuerySQL(template, param);
            }
            log.info("DB2PARA selectSql：{}", selectSql);
            PreparedStatement pstmt = conn.prepareStatement(selectSql, ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            pstmt.setQueryTimeout(QUERY_TIMEOUT);
            pstmt.setFetchSize(FETCH_SIZE);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                for (Column column : columns) {
                    String key = column.getToName();
                    if (StringUtils.isEmpty(key)) {
                        key = column.getFromName();
                    }
                    param.put(key, rs.getString(column.getFromName()));
                }
            }
            param.put("totalTables", 1L + param.getLong("totalTables"));
        } catch (Exception e) {
            throw new TaskException(
                    TaskDetailMessage.labeledLines(e, "说明", "DB2PARA selectSql 生成或执行失败", "selectSql", selectSql));
        } finally {
            DbHelper.closeConnection(conn);
        }
    }
}
