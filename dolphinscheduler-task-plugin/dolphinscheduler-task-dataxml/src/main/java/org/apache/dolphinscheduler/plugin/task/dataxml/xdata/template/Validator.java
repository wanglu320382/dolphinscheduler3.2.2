package org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * @author wanglu
 * @date 2026-04-23
 *
 * <p>校验器配置（对应 {@code validator} 复合类型，JAXB 映射）。
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "validator", propOrder = {
    "param"
})
public class Validator {

    protected List<Param> param;
    @XmlAttribute(name = "type")
    protected String type;

    /**
     * 获取校验参数条目列表。
     * <p>
     * 返回可直接修改的列表引用，可通过 {@code getParam().add(...)} 追加元素。
     * </p>
     *
     * @return {@link Param} 列表
     */
    public List<Param> getParam() {
        if (param == null) {
            param = new ArrayList<Param>();
        }
        return this.param;
    }

    /**
     * 获取type属性的值。
     * 
     * @return 可能的对象类型 {@link String }
     */
    public String getType() {
        return type;
    }

    /**
     * 设置type属性的值。
     * 
     * @param value 允许的对象类型 {@link String }
     */
    public void setType(String value) {
        this.type = value;
    }

}


