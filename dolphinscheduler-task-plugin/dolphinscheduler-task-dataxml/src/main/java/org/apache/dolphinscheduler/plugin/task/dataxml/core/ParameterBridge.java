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

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ParameterBridge {

    private ParameterBridge() {
    }

    public static ParamContext buildParamContext(TaskExecutionContext ctx, AbstractParameters parameters) {
        Map<String, Property> paramsMap = new HashMap<>();
        if (ctx.getPrepareParamsMap() != null) {
            paramsMap.putAll(ctx.getPrepareParamsMap());
        }
        List<Property> localParams = parameters.getLocalParams();
        if (localParams != null) {
            for (Property p : localParams) {
                if (p.getDirect() == Direct.IN) {
                    paramsMap.put(p.getProp(), p);
                }
            }
        }
        Map<String, String> converted = ParameterUtils.convert(paramsMap);
        ParamContext paramContext = new ParamContext();
        converted.forEach((k, v) -> {
            if (v != null) {
                paramContext.put(k, v);
            }
        });
        paramContext.put("totalRecords", 0L);
        paramContext.put("totalTables", 0L);
        return paramContext;
    }
}
