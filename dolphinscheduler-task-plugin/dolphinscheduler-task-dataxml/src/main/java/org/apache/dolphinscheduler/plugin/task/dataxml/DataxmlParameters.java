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

import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.dataxml.util.XDataUtil;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.BusType;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.XdataTemplate;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class DataxmlParameters extends AbstractParameters {

    private String xmlContent;

    @Override
    public boolean checkParameters() {
        if (StringUtils.isEmpty(xmlContent)) {
            return false;
        }
        try {
            XDataUtil.parseXdataTemplates(xmlContent);
            return true;
        } catch (Exception e) {
            log.warn("DataXML 参数校验失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public ResourceParametersHelper getResources() {
        ResourceParametersHelper resources = super.getResources();
        if (StringUtils.isEmpty(xmlContent)) {
            return resources;
        }
        try {
            List<XdataTemplate> templates = XDataUtil.parseXdataTemplates(xmlContent);
            Set<String> names = new HashSet<>();
            for (XdataTemplate template : templates) {
                collectDatasource(names, template.getFromDs(), template.getBusType());
                collectDatasource(names, template.getToDs(), null);
            }
            for (String name : names) {
                DataSourceParameters ref = new DataSourceParameters();
                ref.setName(name);
                ref.setResourceType("DATASOURCE");
                resources.put(ResourceType.DATASOURCE, name.hashCode(), ref);
            }
        } catch (Exception e) {
            log.warn("从 XML 提取数据源名称失败: {}", e.getMessage());
        }
        return resources;
    }

    private void collectDatasource(Set<String> names, String dsKey, String busType) {
        if (StringUtils.isEmpty(dsKey)) {
            return;
        }
        String trimmed = dsKey.trim();
        if (BusType.HTTP2DB.name().equalsIgnoreCase(StringUtils.trimToEmpty(busType))
                && (trimmed.startsWith("http://") || trimmed.startsWith("https://"))) {
            return;
        }
        names.add(trimmed);
    }
}
