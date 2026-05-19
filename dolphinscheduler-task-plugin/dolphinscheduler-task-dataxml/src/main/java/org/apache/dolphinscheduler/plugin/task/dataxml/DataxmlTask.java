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

package org.apache.dolphinscheduler.plugin.task.dataxml;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.dataxml.core.DataxmlExecutor;
import org.apache.dolphinscheduler.plugin.task.dataxml.core.ParamContext;
import org.apache.dolphinscheduler.plugin.task.dataxml.core.ParameterBridge;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataxmlTask extends AbstractTask {

    private DataxmlParameters dataxmlParameters;
    private final TaskExecutionContext taskExecutionContext;

    public DataxmlTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {
        dataxmlParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), DataxmlParameters.class);
        if (dataxmlParameters == null || !dataxmlParameters.checkParameters()) {
            throw new TaskException("DataXML 任务参数无效，请检查 xmlContent");
        }
        log.info("Initialize DataXML task params, xmlContent length={}",
                dataxmlParameters.getXmlContent() != null ? dataxmlParameters.getXmlContent().length() : 0);
    }

    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            ParamContext paramContext = ParameterBridge.buildParamContext(taskExecutionContext, dataxmlParameters);
            DataxmlExecutor executor = new DataxmlExecutor(taskExecutionContext.getResourceParametersHelper());
            Map<String, Object> result = executor.execute(dataxmlParameters.getXmlContent(), paramContext);
            syncVarPool(paramContext);
            Object returnCode = result.get("returnCode");
            if (returnCode instanceof Number && ((Number) returnCode).intValue() != 0) {
                setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
                log.error("DataXML 执行失败: {}", result.get("returnMessage"));
            } else {
                setExitStatusCode(TaskConstants.EXIT_CODE_SUCCESS);
                log.info("DataXML 执行成功: {}", result.get("returnMessage"));
            }
        } catch (Exception e) {
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw new TaskException("DataXML 任务执行失败", e);
        }
    }

    @Override
    public void cancel() throws TaskException {
        // 进程内 JDBC/HTTP，无额外取消逻辑
    }

    @Override
    public AbstractParameters getParameters() {
        return dataxmlParameters;
    }

    private void syncVarPool(ParamContext paramContext) {
        Map<String, String> outMap = new HashMap<>();
        paramContext.forEach((k, v) -> {
            if ("totalRecords".equals(k) || "totalTables".equals(k) || "tableMsg".equals(k)) {
                return;
            }
            if (v != null) {
                outMap.put(k, String.valueOf(v));
            }
        });
        dataxmlParameters.dealOutParam(outMap);
        taskExecutionContext.setVarPool(JSONUtils.toJsonString(dataxmlParameters.getVarPool()));
    }
}
