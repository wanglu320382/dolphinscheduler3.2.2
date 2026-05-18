package org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author wanglu
 * @date 2026-04-29
 *
 * <p>业务类型枚举（对应 {@code busType}）。
 */
@XmlType(name = "busType")
@XmlEnum
public enum BusType {
    DB2DB,
    DB2PARA,
    HTTP2DB;

    /**
     * 返回枚举常量名称。
     *
     * @return 业务类型字符串
     */
    public String value() {
        return name();
    }

    /**
     * 根据字符串转换为业务类型枚举。
     *
     * @param v 业务类型字符串
     * @return 业务类型枚举
     */
    public static BusType fromValue(String v) {
        return valueOf(v);
    }
}


