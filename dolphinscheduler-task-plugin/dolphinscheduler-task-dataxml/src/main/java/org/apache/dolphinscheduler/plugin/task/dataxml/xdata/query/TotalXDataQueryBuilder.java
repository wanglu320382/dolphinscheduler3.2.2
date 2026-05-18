package org.apache.dolphinscheduler.plugin.task.dataxml.xdata.query;

import org.apache.dolphinscheduler.plugin.task.dataxml.core.ParamContext;


import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.XdataTemplate;

/**
 * 全量数据导入查询构建器。
 * <br>一般用于数据量较小的基础信息表、配置表或状态表采集。
 *
 * @author wanglu
 * @date 2026-04-23
 */
public class TotalXDataQueryBuilder extends XDataQueryBuilder {

	private static TotalXDataQueryBuilder instance = null;

	/**
	 * 私有构造，限制外部直接实例化。
	 */
	private TotalXDataQueryBuilder() {
	}

	/**
	 * 获取全量查询构建器单例。
	 *
	 * @return 全量查询构建器
	 */
	public static TotalXDataQueryBuilder getInstance(){
		if(instance == null){
			instance = new TotalXDataQueryBuilder();
		} 
		return instance;
	}

	/**
	 * 全量模式下不追加过滤条件。
	 *
	 * @param template xdata 模板
	 * @param param 运行参数
	 * @return 空字符串
	 */
	@Override
	public String genQueryCondition(XdataTemplate template, ParamContext param) {
		return "";
	}

	/**
	 * 全量模式下不生成清理条件。
	 *
	 * @param template xdata 模板
	 * @param param 运行参数
	 * @return 空字符串
	 */
	@Override
	public String genClearCondition(XdataTemplate template, ParamContext param) {
		return "";
	}

	/**
	 * 全量模式下不生成回滚 SQL。
	 *
	 * @param template xdata 模板
	 * @param param 运行参数
	 * @return 空字符串
	 */
	@Override
	public String genRollBackSQL(XdataTemplate template, ParamContext param) {
		return "";
	}
	
}


