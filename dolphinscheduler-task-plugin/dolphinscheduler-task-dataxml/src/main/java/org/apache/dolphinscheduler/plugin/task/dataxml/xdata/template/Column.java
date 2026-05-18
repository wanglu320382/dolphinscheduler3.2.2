package org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template;


import javax.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wanglu
 * @date 2026-04-23
 *
 * <p>字段映射列（对应 {@code column} 复合类型，JAXB 映射）。
 *
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "column", propOrder = {
    "validator", "column"
})
public class Column {
    @XmlElement(name = "column")
    protected List<Column> column;
    protected Validator validator;
    @XmlAttribute(name = "fromName")
    protected String fromName;
    @XmlAttribute(name = "fromSql")
    protected String fromSql;
    @XmlAttribute(name = "fromType")
    protected String fromType;
    @XmlAttribute(name = "paramValue")
    protected String paramValue;
    @XmlAttribute(name = "condType")
    protected String condType;
    @XmlAttribute(name = "toName")
    protected String toName;
    @XmlAttribute(name = "toType")
    protected String toType;
    @XmlAttribute(name = "seqColumn")
    protected Boolean seqColumn;

    /**
     * 获取字段映射列列表。
     * <p>
     * 返回可直接修改的列表引用，可通过 {@code getColumn().add(...)} 追加元素。
     * </p>
     *
     * @return {@link Column} 列表
     */
    public List<Column> getColumn() {
        if (column == null) {
            column = new ArrayList<Column>();
        }
        return this.column;
    }

    /**
     * 获取validator属性的值。
     * 
     * @return 可能的对象类型 {@link Validator }
     */
    public Validator getValidator() {
        return validator;
    }

    /**
     * 设置validator属性的值。
     * 
     * @param value 允许的对象类型 {@link Validator }
     */
    public void setValidator(Validator value) {
        this.validator = value;
    }

    /**
     * 获取fromName属性的值。
     * 
     * @return 可能的对象类型 {@link String }
     */
    public String getFromName() {
        return fromName;
    }

    /**
     * 设置fromName属性的值。
     * 
     * @param value 允许的对象类型 {@link String }
     */
    public void setFromName(String value) {
        this.fromName = value;
    }

    /**
     * 获取fromSql属性的值。
     * 
     * @return 可能的对象类型 {@link String }
     */
    public String getFromSql() {
        return fromSql;
    }

    /**
     * 设置fromSql属性的值。
     * 
     * @param value 允许的对象类型 {@link String }
     */
    public void setFromSql(String value) {
        this.fromSql = value;
    }

    /**
     * 获取fromType属性的值。
     * 
     * @return 可能的对象类型 {@link String }
     */
    public String getFromType() {
        return fromType;
    }

    /**
     * 设置fromType属性的值。
     * 
     * @param value 允许的对象类型 {@link String }
     */
    public void setFromType(String value) {
        this.fromType = value;
    }

    /**
     * 获取paramValue属性的值。
     *
     * @return 可能的对象类型 {@link String }
     */
    public String getParamValue() {
        return paramValue;
    }

    /**
     * 设置paramValue属性的值。
     *
     * @param value 允许的对象类型 {@link String }
     */
    public void setParamValue(String value) {
        this.paramValue = value;
    }

    /**
     * 获取condType属性的值。
     *
     * @return 可能的对象类型 {@link String }
     */
    public String getCondType() {
        return condType;
    }

    /**
     * 设置condType属性的值。
     *
     * @param value 允许的对象类型 {@link String }
     */
    public void setCondType(String value) {
        this.condType = value;
    }

    /**
     * 获取toName属性的值。
     * 
     * @return 可能的对象类型 {@link String }
     */
    public String getToName() {
        return toName;
    }

    /**
     * 设置toName属性的值。
     * 
     * @param value 允许的对象类型 {@link String }
     */
    public void setToName(String value) {
        this.toName = value;
    }

    /**
     * 获取toType属性的值。
     * 
     * @return 可能的对象类型 {@link String }
     */
    public String getToType() {
        return toType;
    }

    /**
     * 设置toType属性的值。
     * 
     * @param value 允许的对象类型 {@link String }
     */
    public void setToType(String value) {
        this.toType = value;
    }

    /**
     * 获取seqColumn属性的值。
     * 
     * @return 可能的对象类型 {@link Boolean }
     */
    public boolean isSeqColumn() {
        if (seqColumn == null) {
            return false;
        } else {
            return seqColumn;
        }
    }

    /**
     * 设置seqColumn属性的值。
     * 
     * @param value 允许的对象类型 {@link Boolean }
     */
    public void setSeqColumn(Boolean value) {
        this.seqColumn = value;
    }

}


