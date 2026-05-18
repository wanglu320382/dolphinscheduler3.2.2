package org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * @author wanglu
 * @date 2026-04-23
 *
 * <p>数据抓取类型枚举（对应 {@code snatchType}）。
 * 
 */
@XmlType(name = "snatchType")
@XmlEnum
public enum SnatchType {

    TOTAL,
    INCREMENT,
    CONDITION;

    /**
     * 返回枚举常量名称。
     *
     * @return 抓取类型字符串
     */
    public String value() {
        return name();
    }

    /**
     * 根据字符串转换为抓取类型枚举。
     *
     * @param v 抓取类型字符串
     * @return 抓取类型枚举
     */
    public static SnatchType fromValue(String v) {
        return valueOf(v);
    }

}


