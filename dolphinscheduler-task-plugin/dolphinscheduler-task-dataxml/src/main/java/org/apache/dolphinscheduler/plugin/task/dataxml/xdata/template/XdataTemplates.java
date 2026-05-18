package org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template;

import javax.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * @author wanglu
 * @date 2026-04-23
 *
 * <p>XData 模板根节点（对应 {@code xdata-templates} 复合类型，JAXB 映射）。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "xdata-templates", propOrder = {
    "xdataTemplate"
})
@XmlRootElement(name = "xdataTemplates", namespace = "")
public class XdataTemplates {

    @XmlElement(name = "xdata-template", namespace = "http://www.dxml.com/xdata-templates/")
    protected List<XdataTemplate> xdataTemplate;

    /**
     * 获取 XData 模板条目列表。
     * <p>
     * 返回可直接修改的列表引用，可通过 {@code getXdataTemplate().add(...)} 追加元素。
     * </p>
     *
     * @return {@link XdataTemplate} 列表
     */
    public List<XdataTemplate> getXdataTemplate() {
        if (xdataTemplate == null) {
            xdataTemplate = new ArrayList<XdataTemplate>();
        }
        return this.xdataTemplate;
    }

}


