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

package org.apache.dolphinscheduler.plugin.task.dataxml.util;

import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.dataxml.core.ParamContext;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.Column;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.FromType;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonNodeHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonNodeHelper() {
    }

    public static JsonNode getNode(JsonNode root, String path) {
        if (root == null || StringUtils.isEmpty(path)) {
            return root;
        }
        JsonNode cur = root;
        for (String p : path.split("\\.")) {
            if (cur == null || cur.isNull()) {
                return null;
            }
            cur = cur.get(p);
        }
        return cur;
    }

    public static Object jsonNodeValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        if (node.isNumber()) {
            return node.numberValue();
        }
        return OBJECT_MAPPER.convertValue(node, Object.class);
    }

    public static Object convertByColumn(JsonNode source, Column column, ParamContext param) throws TaskException {
        String rawParamValue = PlaceholderUtils.replace(column.getParamValue(), param, false);
        if (StringUtils.isNotEmpty(PlaceholderUtils.trim(rawParamValue))) {
            return rawParamValue;
        }
        String fromType = PlaceholderUtils.trim(column.getFromType());
        boolean hasChildren = !column.getColumn().isEmpty();
        boolean isObjectMapping = hasChildren && StringUtils.isEmpty(fromType);
        if (FromType.json.value().equalsIgnoreCase(fromType) || isObjectMapping) {
            if (column.getColumn().isEmpty()) {
                if (source == null || source.isNull()) {
                    return null;
                }
                return OBJECT_MAPPER.convertValue(source, Map.class);
            }
            ParamContext out = new ParamContext();
            for (Column child : column.getColumn()) {
                String childFrom = PlaceholderUtils.replace(child.getFromName(), param, false);
                JsonNode childNode = getNode(source, childFrom);
                out.put(resolveColumnOutputName(child), convertByColumn(childNode, child, param));
            }
            return out;
        }
        if (FromType.array.value().equalsIgnoreCase(fromType)) {
            if (source == null || source.isNull() || !source.isArray()) {
                return new ArrayList<>();
            }
            List<Object> out = new ArrayList<>();
            if (column.getColumn().isEmpty()) {
                for (JsonNode item : source) {
                    out.add(jsonNodeValue(item));
                }
                return out;
            }
            for (JsonNode item : source) {
                ParamContext row = new ParamContext();
                for (Column child : column.getColumn()) {
                    String childFrom = PlaceholderUtils.replace(child.getFromName(), param, false);
                    JsonNode childNode = getNode(item, childFrom);
                    row.put(resolveColumnOutputName(child), convertByColumn(childNode, child, param));
                }
                out.add(row);
            }
            return out;
        }
        return jsonNodeValue(source);
    }

    public static Object getMapCellValue(JsonNode row, Column column, ParamContext param) throws TaskException {
        if (FromType.javabean.value().equalsIgnoreCase(PlaceholderUtils.trim(column.getFromType()))) {
            throw new TaskException("fromType=javabean 在 DataXML 插件中不支持");
        }
        String fromName = PlaceholderUtils.replace(column.getFromName(), param, false);
        JsonNode val = getNode(row, fromName);
        return convertByColumn(val, column, param);
    }

    private static String resolveColumnOutputName(Column column) {
        String toName = PlaceholderUtils.trim(column.getToName());
        if (StringUtils.isNotEmpty(toName)) {
            return toName;
        }
        return PlaceholderUtils.trim(column.getFromName());
    }

    public static Map<String, Object> buildRequestParams(List<Column> condColumns, ParamContext param)
            throws TaskException {
        Map<String, Object> body = new HashMap<>();
        for (Column column : condColumns) {
            String key = resolvePropertyName(column);
            String rawValue = PlaceholderUtils.replace(column.getParamValue(), param, false);
            if (StringUtils.isNotEmpty(PlaceholderUtils.trim(rawValue))) {
                body.put(key, rawValue);
                continue;
            }
            String rawFrom = column.getFromName();
            String wholePath = PlaceholderUtils.extractWholePlaceholderPath(rawFrom);
            if (wholePath != null) {
                body.put(key, PlaceholderUtils.getParamValueByPath(param, wholePath));
            }
        }
        return body;
    }

    private static String resolvePropertyName(Column column) {
        String toName = PlaceholderUtils.trim(column.getToName());
        if (StringUtils.isNotEmpty(toName)) {
            return toName;
        }
        return PlaceholderUtils.trim(column.getFromName());
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> extractPageData(JsonNode root, String listPath) throws TaskException {
        if (root == null || root.isNull()) {
            return new ArrayList<>();
        }
        JsonNode listNode;
        if (StringUtils.isNotEmpty(listPath)) {
            listNode = getNode(root, listPath);
        } else {
            JsonNode data = root.get("data");
            if (data != null && !data.isNull()) {
                listNode = data.get("pageData");
                if (listNode == null || listNode.isNull()) {
                    listNode = data.isArray() ? data : root;
                }
            } else {
                listNode = root.isArray() ? root : root.get("pageData");
            }
        }
        if (listNode == null || listNode.isNull()) {
            return new ArrayList<>();
        }
        if (!listNode.isArray()) {
            throw new TaskException("响应数据列表不是数组类型，路径：" + listPath);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (JsonNode item : listNode) {
            result.add(OBJECT_MAPPER.convertValue(item, Map.class));
        }
        return result;
    }

    public static void assertHttpResponseSuccess(JsonNode root) throws TaskException {
        if (root == null || root.isNull()) {
            return;
        }
        JsonNode codeNode = root.get("code");
        if (codeNode == null || codeNode.isNull()) {
            return;
        }
        int code = codeNode.asInt();
        if (code == 0 || code == 200) {
            return;
        }
        JsonNode msgNode = root.get("msg");
        String msg = msgNode != null && !msgNode.isNull() ? msgNode.asText() : "";
        throw new TaskException(StringUtils.isEmpty(msg) ? "HTTP 调用失败（code=" + code + "）" : msg);
    }
}
