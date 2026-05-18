package org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template;


import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "fromType")
@XmlEnum
public enum FromType {
    // string bean
    javabean,
    // 入参
    cond,
    // 序列
    seq,
    json,
    array;

    public String value() {
        return name();
    }

    public static FromType fromValue(String v) {
        return valueOf(v);
    }

}


