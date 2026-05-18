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

public final class TaskDetailMessage {

    private TaskDetailMessage() {
    }

    public static String executeFailReturnMessage(String templateName, String detail) {
        String name = templateName == null ? "" : templateName;
        String msg = detail == null ? "" : detail;
        return String.format("执行失败！%n配置名称：%s%n%s", name, msg);
    }

    public static String executeSuccessReturnMessage(long totalTables, long totalRecords, String tableMsg) {
        String tm = tableMsg == null ? "" : tableMsg;
        return String.format("执行成功！\rTotal %d objects\r      %d Records(%s)", totalTables, totalRecords, tm);
    }

    public static String labeledLines(Throwable cause, String... labelValues) {
        String head = PlaceholderUtils.localizedOrMessage(cause);
        if (labelValues == null || labelValues.length == 0) {
            return head;
        }
        StringBuilder sb = new StringBuilder(head);
        for (int i = 0; i + 1 < labelValues.length; i += 2) {
            String label = labelValues[i] != null ? labelValues[i] : "";
            String value = labelValues[i + 1] != null ? labelValues[i + 1] : "";
            sb.append('\n').append(label).append("：").append(value);
        }
        return sb.toString();
    }
}
