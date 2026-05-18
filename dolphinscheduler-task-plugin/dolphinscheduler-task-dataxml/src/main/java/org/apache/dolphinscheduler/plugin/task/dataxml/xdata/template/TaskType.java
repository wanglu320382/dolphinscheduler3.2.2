package org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * @author wanglu
 * @date 2026-04-23
 *
 * <p>任务类型枚举（对应 {@code taskType}）。
 *
 * 
 */
@XmlType(name = "taskType")
@XmlEnum
public enum TaskType {
    INSERT,
    UPDATE,
    SQL,
    PARA;

    /**
     * 返回枚举常量名称。
     *
     * @return 任务类型字符串
     */
    public String value() {
        return name();
    }

    /**
     * 根据字符串转换为任务类型枚举。
     *
     * @param v 任务类型字符串
     * @return 任务类型枚举
     */
    public static TaskType fromValue(String v) {
        return valueOf(v);
    }

}


