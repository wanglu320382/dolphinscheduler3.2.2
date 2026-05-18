package org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template;

import javax.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * @author wanglu
 * @date 2026-04-29
 *
 * <p>单条 XData 采集模板（对应 {@code xdata-template} 复合类型，JAXB 映射）。
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "xdata-template", propOrder = {
    "column","updateCond"
})
public class XdataTemplate {

    @XmlElement(required = false, name = "column", namespace = "http://www.dxml.com/xdata-templates/")
    protected List<Column> column;
    @XmlElement(required = false, name = "updateCond", namespace = "http://www.dxml.com/xdata-templates/")
    protected List<Column> updateCond;
    @XmlAttribute(name = "snatchType")
    protected SnatchType snatchType;
    @XmlAttribute(name = "taskType")
    protected TaskType taskType;
    @XmlAttribute(name = "busType")
    protected String busType;
    @XmlAttribute(name = "fromDs")
    protected String fromDs;
    @XmlAttribute(name = "fromSchema")
    protected String fromSchema;
    @XmlAttribute(name = "fromObjectName")
    protected String fromObjectName;
    @XmlAttribute(name = "toDs")
    protected String toDs;
    @XmlAttribute(name = "toObjectName")
    protected String toObjectName;
    @XmlAttribute(name = "execSql")
    protected String execSql;
    @XmlAttribute(name = "clearOldData")
    protected Boolean clearOldData;
    @XmlAttribute(name = "clearOldDataType")
    protected ClearOldDataType clearOldDataType;
    @XmlAttribute(name = "queryCond")
    protected String queryCond;
    @XmlAttribute(name = "clearCond")
    protected String clearCond;
    @XmlAttribute(name = "expressionCond")
    protected String expressionCond;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "illegalDataStrategy")
    protected String illegalDataStrategy;



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
     * 获取更新条件字段列表。
     *
     * @return 更新条件列表
     */
    public List<Column> getUpdateCond() {
        if (updateCond == null) {
            updateCond = new ArrayList<Column>();
        }
        return this.updateCond;
    }

    /**
     * 获取snatchType属性的值。
     * 
     * @return 可能的对象类型 {@link SnatchType }
     */
    public SnatchType getSnatchType() {
        return snatchType;
    }

    /**
     * 设置snatchType属性的值。
     * 
     * @param value 允许的对象类型 {@link SnatchType }
     */
    public void setSnatchType(SnatchType value) {
        this.snatchType = value;
    }

    /**
     * 获取taskType属性的值。
     * 
     * @return 可能的对象类型 {@link TaskType }
     */
    public TaskType getTaskType() {
        return taskType;
    }

    /**
     * 设置taskType属性的值。
     * 
     * @param value 允许的对象类型 {@link TaskType }
     */
    public void setTaskType(TaskType value) {
        this.taskType = value;
    }

    /**
     * 获取 busType 属性的值。
     *
     * @return 业务类型（如 REMOTE2DB、REMOTE、DB2DB、BEAN、DB2PARA）
     */
    public String getBusType() {
        return busType;
    }

    /**
     * 设置 busType 属性的值。
     *
     * @param value 业务类型
     */
    public void setBusType(String value) {
        this.busType = value;
    }

    /**
     * 获取fromDs属性的值。
     * 
     * @return 可能的对象类型 {@link String }
     */
    public String getFromDs() {
        return fromDs;
    }

    /**
     * 设置fromDs属性的值。
     * 
     * @param value 允许的对象类型 {@link String }
     */
    public void setFromDs(String value) {
        this.fromDs = value;
    }

    /**
     * 获取fromSchema属性的值。
     * 
     * @return 可能的对象类型 {@link String }
     */
    public String getFromSchema() {
        return fromSchema;
    }

    /**
     * 设置fromSchema属性的值。
     * 
     * @param value 允许的对象类型 {@link String }
     */
    public void setFromSchema(String value) {
        this.fromSchema = value;
    }

    /**
     * 获取fromObjectName属性的值。
     * 
     * @return 可能的对象类型 {@link String }
     */
    public String getFromObjectName() {
        return fromObjectName;
    }

    /**
     * 设置fromObjectName属性的值。
     * 
     * @param value 允许的对象类型 {@link String }
     */
    public void setFromObjectName(String value) {
        this.fromObjectName = value;
    }

    /**
     * 获取toDs属性的值。
     * 
     * @return 可能的对象类型 {@link String }
     */
    public String getToDs() {
        return toDs;
    }

    /**
     * 设置toDs属性的值。
     * 
     * @param value 允许的对象类型 {@link String }
     */
    public void setToDs(String value) {
        this.toDs = value;
    }

    /**
     * 获取toObjectName属性的值。
     * 
     * @return 可能的对象类型 {@link String }
     */
    public String getToObjectName() {
        return toObjectName;
    }

    /**
     * 设置toObjectName属性的值。
     * 
     * @param value 允许的对象类型 {@link String }
     */
    public void setToObjectName(String value) {
        this.toObjectName = value;
    }

    /**
     * 获取execSql属性的值。
     *
     * @return 可能的对象类型 {@link String }
     */
    public String getExecSql() {
        return execSql;
    }

    /**
     * 设置execSql属性的值。
     *
     * @param value 允许的对象类型 {@link String }
     */
    public void setExecSql(String value) {
        this.execSql = value;
    }

    /**
     * 获取clearOldData属性的值。
     * 
     * @return 可能的对象类型 {@link Boolean }
     */
    public boolean isClearOldData() {
        if (clearOldData == null) {
            return false;
        } else {
            return clearOldData;
        }
    }

    /**
     * 设置clearOldData属性的值。
     * 
     * @param value 允许的对象类型 {@link Boolean }
     */
    public void setClearOldData(Boolean value) {
        this.clearOldData = value;
    }

    /**
     * 获取clearOldDataType属性的值。
     * 
     * @return 可能的对象类型 {@link ClearOldDataType }
     */
    public ClearOldDataType getClearOldDataType() {
        return clearOldDataType;
    }

    /**
     * 设置clearOldDataType属性的值。
     * 
     * @param value 允许的对象类型 {@link ClearOldDataType }
     */
    public void setClearOldDataType(ClearOldDataType value) {
        this.clearOldDataType = value;
    }

    /**
     * 获取queryCond属性的值。
     * 
     * @return 可能的对象类型 {@link String }
     */
    public String getQueryCond() {
        return queryCond;
    }

    /**
     * 设置queryCond属性的值。
     * 
     * @param value 允许的对象类型 {@link String }
     */
    public void setQueryCond(String value) {
        this.queryCond = value;
    }

    /**
     * 获取 clearCond 属性的值。
     *
     * @return 可能的对象类型 {@link String }
     */
    public String getClearCond() {
        return clearCond;
    }

    /**
     * 设置 clearCond 属性的值。
     *
     * @param value 允许的对象类型 {@link String }
     */
    public void setClearCond(String value) {
        this.clearCond = value;
    }

    /**
     * 获取expressionCond属性的值。
     *
     * @return 可能的对象类型 {@link String }
     */
    public String getExpressionCond() {
        return expressionCond;
    }

    /**
     * 设置 expressionCond 属性的值。
     *
     * @param value 允许的对象类型 {@link String }
     */
    public void setExpressionCond(String value) {
        this.expressionCond = value;
    }

    /**
     * 获取模板名称属性的值。
     *
     * @return 模板名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置模板名称属性的值。
     *
     * @param value 模板名称
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * 兼容历史命名，内部委托给标准 setter。
     *
     * @param value expressionCond
     * @deprecated 请使用 {@link #setExpressionCond(String)}
     */
    @Deprecated
    public void setgetExpressionCond(String value) {
        setExpressionCond(value);
    }




    /**
     * 获取illegalDataStrategy属性的值。
     * 
     * @return 可能的对象类型 {@link String }
     */
    public String getIllegalDataStrategy() {
        return illegalDataStrategy;
    }

    /**
     * 设置illegalDataStrategy属性的值。
     * 
     * @param value 允许的对象类型 {@link String }
     */
    public void setIllegalDataStrategy(String value) {
        this.illegalDataStrategy = value;
    }

}


