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

import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaderContentType;
import org.apache.dolphinscheduler.common.model.OkHttpRequestHeaders;
import org.apache.dolphinscheduler.common.model.OkHttpResponse;
import org.apache.dolphinscheduler.common.utils.OkHttpUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.dataxml.core.ParamContext;
import org.apache.dolphinscheduler.plugin.task.dataxml.datasource.DataSourceResolver;
import org.apache.dolphinscheduler.plugin.task.dataxml.util.ConvertUtil;
import org.apache.dolphinscheduler.plugin.task.dataxml.util.DbHelper;
import org.apache.dolphinscheduler.plugin.task.dataxml.util.JsonNodeHelper;
import org.apache.dolphinscheduler.plugin.task.dataxml.util.PlaceholderUtils;
import org.apache.dolphinscheduler.plugin.task.dataxml.util.TaskDetailMessage;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.query.XDataQueryBuilder;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.query.XdataQueryBuilderFactory;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.Column;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.XdataTemplate;

import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@RequiredArgsConstructor
public class Http2DbHandler {

    private static final int CONNECT_TIMEOUT_MS = 60000;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final DataSourceResolver dataSourceResolver;

    public void execute(XdataTemplate template, ParamContext param) throws Exception {
        String url = PlaceholderUtils.replace(template.getFromDs(), param, false);
        if (StringUtils.isEmpty(url)) {
            throw new TaskException("HTTP2DB 的 fromDs 须配置 HTTP 地址");
        }
        List<Column> mapColumns = template.getColumn();
        if (mapColumns.isEmpty()) {
            throw new TaskException("未配置字段映射 column");
        }
        List<Column> condColumns = template.getUpdateCond();
        Connection toConn = dataSourceResolver.getConnection(template.getToDs());
        try {
            List<Class<?>> toColumnClzs = getToColumnClzs(toConn, template, mapColumns);
            String insertSql = genInsertSql(template.getToObjectName(), mapColumns);
            XDataQueryBuilder queryBuilder = XdataQueryBuilderFactory.genXdataQueryBuilder(template);
            String deleteSql = queryBuilder.genClearOldDataSQLByCond(template, param);
            if (StringUtils.isNotEmpty(deleteSql)) {
                log.info("HTTP2DB 生成旧数据清理 SQL：{}", deleteSql);
            }
            PreparedStatement insertPstmt = toConn.prepareStatement(insertSql);
            int total = 0;
            int commitCount = 0;
            while (true) {
                OkHttpResponse response = sendRequest(url, condColumns, param);
                if (response.getStatusCode() < 200 || response.getStatusCode() >= 300) {
                    throw new TaskException("HTTP 请求失败，statusCode=" + response.getStatusCode());
                }
                JsonNode root = OBJECT_MAPPER.readTree(response.getBody());
                JsonNodeHelper.assertHttpResponseSuccess(root);
                // 列表路径：fromObjectName 可配置 JSON 锚点（如 data.pageData），未配置则走默认解析
                String listPath = PlaceholderUtils.replace(template.getFromObjectName(), param, false);
                List<Map<String, Object>> pageData = JsonNodeHelper.extractPageData(root, listPath);
                if (pageData.isEmpty()) {
                    break;
                }
                if (total == 0 && StringUtils.isNotEmpty(deleteSql)) {
                    log.info("HTTP2DB 执行旧数据清理 SQL：{}", deleteSql);
                    DbHelper.executeUpdate(toConn, deleteSql, null);
                    toConn.commit();
                }
                for (Map<String, Object> row : pageData) {
                    JsonNode rowNode = OBJECT_MAPPER.valueToTree(row);
                    for (int i = 0; i < mapColumns.size(); i++) {
                        Object raw = JsonNodeHelper.getMapCellValue(rowNode, mapColumns.get(i), param);
                        insertPstmt.setObject(i + 1, ConvertUtil.convertToObject(raw, toColumnClzs.get(i)));
                    }
                    insertPstmt.addBatch();
                    total++;
                    commitCount++;
                    if (commitCount >= 500) {
                        insertPstmt.executeBatch();
                        insertPstmt.clearBatch();
                        toConn.commit();
                        commitCount = 0;
                    }
                }
                if (!increasePageNum(condColumns)) {
                    break;
                }
            }
            if (commitCount > 0) {
                insertPstmt.executeBatch();
                insertPstmt.clearBatch();
                toConn.commit();
            }
            param.put("totalTables", 1L + param.getLong("totalTables"));
            param.put("totalRecords", total + param.getLong("totalRecords"));
            PlaceholderUtils.appendTableMsg(param, template.getToObjectName(), total);
        } catch (TaskException e) {
            throw e;
        } catch (Exception e) {
            throw new TaskException(PlaceholderUtils.localizedOrMessage(e));
        } finally {
            DbHelper.closeConnection(toConn);
        }
    }

    private OkHttpResponse sendRequest(String url, List<Column> condColumns, ParamContext param) throws Exception {
        Map<String, Object> body = JsonNodeHelper.buildRequestParams(condColumns, param);
        OkHttpRequestHeaders headers = new OkHttpRequestHeaders();
        headers.setOkHttpRequestHeaderContentType(OkHttpRequestHeaderContentType.APPLICATION_JSON);
        return OkHttpUtils.post(url, headers, null, body, CONNECT_TIMEOUT_MS, CONNECT_TIMEOUT_MS, CONNECT_TIMEOUT_MS);
    }

    private String genInsertSql(String tableName, List<Column> mapColumns) throws TaskException {
        if (StringUtils.isEmpty(tableName)) {
            throw new TaskException("toObjectName 不能为空");
        }
        StringBuilder cols = new StringBuilder();
        StringBuilder vals = new StringBuilder();
        for (Column column : mapColumns) {
            String toName = StringUtils.isEmpty(column.getToName()) ? column.getFromName() : column.getToName();
            if (StringUtils.isEmpty(toName)) {
                throw new TaskException("字段映射缺少 toName/fromName");
            }
            cols.append(toName).append(", ");
            vals.append("?, ");
        }
        cols.setLength(cols.length() - 2);
        vals.setLength(vals.length() - 2);
        return "insert into " + tableName + " (" + cols + ") values (" + vals + ")";
    }

    private List<Class<?>> getToColumnClzs(Connection conn, XdataTemplate template, List<Column> mapColumns)
            throws SQLException, TaskException {
        StringBuilder colBuilder = new StringBuilder();
        for (Column column : mapColumns) {
            String toName = StringUtils.isEmpty(column.getToName()) ? column.getFromName() : column.getToName();
            colBuilder.append(toName).append(", ");
        }
        colBuilder.setLength(colBuilder.length() - 2);
        String sql = "select " + colBuilder + " from " + template.getToObjectName() + " where 1<>1";
        List<Class<?>> columnClzs = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSetMetaData rsmd = pstmt.executeQuery().getMetaData();
            for (int i = 0, n = rsmd.getColumnCount(); i < n; i++) {
                columnClzs.add(PlaceholderUtils.resolveClass(rsmd.getColumnClassName(i + 1)));
            }
        } catch (Exception e) {
            throw new TaskException(TaskDetailMessage.labeledLines(e, "toTableClmQuerySql", sql));
        }
        return columnClzs;
    }

    private boolean increasePageNum(List<Column> condColumns) {
        for (Column column : condColumns) {
            String condName = PlaceholderUtils.trim(column.getToName());
            if (StringUtils.isEmpty(condName)) {
                condName = PlaceholderUtils.trim(column.getFromName());
            }
            if (Objects.equals(condName, "pageNum")) {
                int current = parsePositiveInt(column.getParamValue(), 1);
                column.setParamValue(String.valueOf(current + 1));
                return true;
            }
        }
        return false;
    }

    private int parsePositiveInt(String text, int defaultValue) {
        if (StringUtils.isEmpty(text)) {
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(text.trim());
            return value > 0 ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
