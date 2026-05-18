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

package org.apache.dolphinscheduler.plugin.task.dataxml.core;

import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.plugin.task.dataxml.datasource.DataSourceResolver;
import org.apache.dolphinscheduler.plugin.task.dataxml.handler.Db2DbHandler;
import org.apache.dolphinscheduler.plugin.task.dataxml.handler.Db2ParaHandler;
import org.apache.dolphinscheduler.plugin.task.dataxml.handler.Http2DbHandler;
import org.apache.dolphinscheduler.plugin.task.dataxml.util.PlaceholderUtils;
import org.apache.dolphinscheduler.plugin.task.dataxml.util.TaskDetailMessage;
import org.apache.dolphinscheduler.plugin.task.dataxml.util.XDataUtil;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.BusType;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.XdataTemplate;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataxmlExecutor {

    private final DataSourceResolver dataSourceResolver;

    public DataxmlExecutor(ResourceParametersHelper resourceHelper) {
        this.dataSourceResolver = new DataSourceResolver(resourceHelper);
    }

    public Map<String, Object> execute(String xmlContent, ParamContext param) {
        Map<String, Object> result = new HashMap<>();
        String currentTemplateName = "";
        Db2ParaHandler db2ParaHandler = new Db2ParaHandler(dataSourceResolver);
        Db2DbHandler db2DbHandler = new Db2DbHandler(dataSourceResolver);
        Http2DbHandler http2DbHandler = new Http2DbHandler(dataSourceResolver);
        try {
            List<XdataTemplate> templates = XDataUtil.parseXdataTemplates(xmlContent);
            for (XdataTemplate template : templates) {
                currentTemplateName = template.getName();
                if (!PlaceholderUtils.shouldExecuteByExpressionCond(template.getExpressionCond(), param)) {
                    log.info("跳过模板 {}，expressionCond 不满足", currentTemplateName);
                    continue;
                }
                BusType busType = resolveBusType(template);
                switch (busType) {
                    case DB2PARA:
                        db2ParaHandler.execute(template, param);
                        break;
                    case DB2DB:
                        db2DbHandler.execute(template, param);
                        break;
                    case HTTP2DB:
                        http2DbHandler.execute(template, param);
                        break;
                    default:
                        throw new TaskException("不支持的 busType：" + template.getBusType());
                }
            }
            result.put("returnCode", 0);
            result.put("returnMessage", TaskDetailMessage.executeSuccessReturnMessage(
                    param.getLong("totalTables"), param.getLong("totalRecords"), param.getString("tableMsg")));
        } catch (TaskException e) {
            result.put("returnCode", 1);
            result.put("returnMessage",
                    TaskDetailMessage.executeFailReturnMessage(currentTemplateName, PlaceholderUtils.localizedOrMessage(e)));
        } catch (Exception e) {
            result.put("returnCode", 1);
            result.put("returnMessage",
                    TaskDetailMessage.executeFailReturnMessage(currentTemplateName, PlaceholderUtils.localizedOrMessage(e)));
        }
        return result;
    }

    private BusType resolveBusType(XdataTemplate template) throws TaskException {
        String bt = PlaceholderUtils.trim(template.getBusType());
        if (StringUtils.isEmpty(bt)) {
            throw new TaskException("busType 不能为空");
        }
        try {
            return BusType.valueOf(bt.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new TaskException("不支持的 busType：" + bt);
        }
    }
}
