package org.apache.dolphinscheduler.plugin.task.dataxml.xdata.query;

import org.apache.dolphinscheduler.plugin.task.dataxml.core.ParamContext;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.SnatchType;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.XdataTemplate;

/**
 * XData 查询构建器工厂类，按抓取类型返回对应实现。
 *
 * @author wanglu
 * @date 2026-04-23
 */
public class XdataQueryBuilderFactory {
	
	/**
	 * 根据模板抓取类型创建查询构建器。
	 *
	 * @param template xdata 模板
	 * @return 查询构建器
	 */
	public static XDataQueryBuilder genXdataQueryBuilder(XdataTemplate template) {
		
		XDataQueryBuilder builder = null;
		
		SnatchType type = template.getSnatchType();
		if (type == null){
			// 默认是 SnatchType.TOTAL
			builder = TotalXDataQueryBuilder.getInstance();
		} else {
			switch (type) {
				case TOTAL:
					builder = TotalXDataQueryBuilder.getInstance();
					break;
				case INCREMENT:
					builder = IncXDataQueryBuilder.getInstance();
					break;
				case CONDITION:
					builder = CondXDataQueryBuilder.getInstance();
					break;
				default:
					// 默认是 SnatchType.TOTAL
					builder = TotalXDataQueryBuilder.getInstance();
			}
		}
		
		return builder;

	}
}


