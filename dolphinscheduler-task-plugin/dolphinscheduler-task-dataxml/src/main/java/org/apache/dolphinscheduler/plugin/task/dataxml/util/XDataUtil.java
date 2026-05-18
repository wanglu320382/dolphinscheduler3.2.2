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
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.XdataTemplate;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.XdataTemplates;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXB;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class XDataUtil {

    private XDataUtil() {
    }

    public static List<XdataTemplate> parseXdataTemplates(String xmlContent) throws TaskException {
        XdataTemplates root = unmarshalXml(xmlContent);
        return new ArrayList<>(root.getXdataTemplate());
    }

    private static XdataTemplates unmarshalXml(String xmlContent) throws TaskException {
        byte[] bytes = xmlContent.getBytes(StandardCharsets.UTF_8);
        try (InputStream in = new ByteArrayInputStream(bytes)) {
            return JAXB.unmarshal(in, XdataTemplates.class);
        } catch (Exception ex) {
            log.error("加载 xml 字符串失败", ex);
            String m = ex.getLocalizedMessage();
            if (StringUtils.isBlank(m)) {
                m = ex.getMessage();
            }
            throw new TaskException("解析 XML 失败: " + (m != null ? m : ex.getClass().getSimpleName()));
        }
    }
}
