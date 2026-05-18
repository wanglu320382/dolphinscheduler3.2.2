package org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author wanglu
 * @date 2026-04-23
 *
 * 清理旧数据策略枚举。
 * 
 */
@XmlType(name = "clearOldDataType")
@XmlEnum
public enum ClearOldDataType {

    ALL,
    CONDITION;

    /**
     * 返回枚举常量名称。
     *
     * @return 清理策略字符串
     */
    public String value() {
        return name();
    }

    /**
     * 根据字符串转换为清理策略枚举。
     *
     * @param v 清理策略字符串
     * @return 清理策略枚举
     */
    public static ClearOldDataType fromValue(String v) {
        return valueOf(v);
    }

}


