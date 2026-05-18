package org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author wanglu
 * @date 2026-04-23
 *
 * <p>键值参数（对应 {@code param} 复合类型，JAXB 映射）。
 *
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "param")
public class Param {

    @XmlAttribute(name = "key")
    protected String key;
    @XmlAttribute(name = "value")
    protected String value;

    /**
     * 获取key属性的值。
     * 
     * @return 可能的对象类型 {@link String }
     */
    public String getKey() {
        return key;
    }

    /**
     * 设置key属性的值。
     * 
     * @param value 允许的对象类型 {@link String }
     */
    public void setKey(String value) {
        this.key = value;
    }

    /**
     * 获取value属性的值。
     * 
     * @return 可能的对象类型 {@link String }
     */
    public String getValue() {
        return value;
    }

    /**
     * 设置value属性的值。
     * 
     * @param value 允许的对象类型 {@link String }
     */
    public void setValue(String value) {
        this.value = value;
    }

}


