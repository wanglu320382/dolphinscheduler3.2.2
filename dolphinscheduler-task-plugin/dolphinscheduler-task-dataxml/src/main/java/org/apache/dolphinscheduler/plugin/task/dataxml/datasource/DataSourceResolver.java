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

package org.apache.dolphinscheduler.plugin.task.dataxml.datasource;

import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceClientProvider;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.AbstractResourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataSourceResolver {

    private final ResourceParametersHelper resourceHelper;

    public DataSourceResolver(ResourceParametersHelper resourceHelper) {
        this.resourceHelper = resourceHelper;
    }

    public Connection getConnection(String dsKey) throws TaskException {
        if (StringUtils.isEmpty(dsKey)) {
            throw new TaskException("数据源标识不能为空");
        }
        int id = resolveDatasourceId(dsKey.trim());
        DataSourceParameters params = (DataSourceParameters) resourceHelper
                .getResourceParameters(ResourceType.DATASOURCE, id);
        if (params == null) {
            throw new TaskException("未找到数据源配置，dsKey=" + dsKey);
        }
        DbType dbType = params.getType();
        if (dbType == null) {
            throw new TaskException("数据源类型未配置，dsKey=" + dsKey);
        }
        BaseConnectionParam connectionParam = (BaseConnectionParam) DataSourceUtils
                .buildConnectionParams(dbType, params.getConnectionParams());
        try {
            Connection conn = DataSourceClientProvider.getAdHocConnection(dbType, connectionParam);
            conn.setAutoCommit(false);
            return conn;
        } catch (SQLException | ExecutionException e) {
            log.error("创建数据库连接失败，dsKey={}", dsKey, e);
            throw new TaskException("创建数据库连接失败：" + e.getMessage(), e);
        }
    }

    /**
     * 将 dsKey（t_ds_datasource.name）解析为数据源 id。
     */
    private int resolveDatasourceId(String dsKey) throws TaskException {
        Map<Integer, AbstractResourceParameters> datasourceMap =
                resourceHelper.getResourceMap(ResourceType.DATASOURCE);
        if (datasourceMap == null || datasourceMap.isEmpty()) {
            throw new TaskException("未找到数据源配置，name=" + dsKey);
        }
        for (Map.Entry<Integer, AbstractResourceParameters> entry : datasourceMap.entrySet()) {
            if (!(entry.getValue() instanceof DataSourceParameters)) {
                continue;
            }
            DataSourceParameters params = (DataSourceParameters) entry.getValue();
            if (dsKey.equals(params.getName())) {
                return entry.getKey();
            }
        }
        throw new TaskException("未找到数据源配置，name=" + dsKey);
    }
}
