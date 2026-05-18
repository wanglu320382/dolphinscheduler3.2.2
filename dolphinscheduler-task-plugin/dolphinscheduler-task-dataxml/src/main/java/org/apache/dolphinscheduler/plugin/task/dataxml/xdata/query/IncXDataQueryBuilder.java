package org.apache.dolphinscheduler.plugin.task.dataxml.xdata.query;

import org.apache.dolphinscheduler.plugin.task.dataxml.core.ParamContext;

import org.apache.commons.lang3.StringUtils;

import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.Column;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.XdataTemplate;
import lombok.extern.slf4j.Slf4j;


@Slf4j
/**
 * 增量数据导入查询构建器，一般用于业务流水表的数据采集。
 * <br>依赖于源表存在自增性字段，例如序列号SerialNo。
 * <p>参见配置文件中ns2:column属性： seqColumn="true"</p>
 *
 * @author wanglu
 * @date 2026-04-23
 */
public class IncXDataQueryBuilder extends XDataQueryBuilder {
	private static IncXDataQueryBuilder instance = null;

	/**
	 * 私有构造，限制外部直接实例化。
	 */
	private IncXDataQueryBuilder() {
	}

	/**
	 * 获取增量查询构建器单例。
	 *
	 * @return 增量查询构建器
	 */
	public static IncXDataQueryBuilder getInstance(){
		if(instance == null){
			instance = new IncXDataQueryBuilder();
		}
		return instance;
	}

	/**
	 * 基于序列字段生成增量查询条件。
	 *
	 * @param template xdata 模板
	 * @param param 运行参数
	 * @return 增量查询条件
	 */
	@Override
	public String genQueryCondition(XdataTemplate template, ParamContext param) throws TaskException {
		Column clm = getSeqColumn(template.getColumn());
		if(clm == null){
			throw new TaskException(template.getFromDs() + "." + template.getFromObjectName() + "->" + template.getToDs() + "." + template.getToObjectName() + "没有指定增量导入字段");
		}
		long serialNo = param.getLong("rollBackSeriaNo");
		return clm.getFromName() + " > " + serialNo;
	}

	/**
	 * 生成增量模式回滚 SQL。
	 *
	 * @param template xdata 模板
	 * @param param 运行参数
	 * @return 回滚 SQL；无条件时返回空串
	 */
	@Override
	public String genRollBackSQL(XdataTemplate template, ParamContext param) throws TaskException {
		String queryCond = genQueryCondition(template, param);
		if (StringUtils.isEmpty(queryCond)) {
			return "";
		}
		return "delete from "
				+ template.getToObjectName()
				+ " where "
				+ queryCond;
	}

	/**
	 * 生成增量模式清理条件。
	 *
	 * @param template xdata 模板
	 * @param param 运行参数
	 * @return 清理条件
	 */
	@Override
	public String genClearCondition(XdataTemplate template, ParamContext param) throws TaskException {
		String clearCond = "";
		if (!StringUtils.isEmpty(template.getClearCond())) {
			clearCond = template.getClearCond();
		} else {
			clearCond = genQueryCondition(template, param);
		}
		return clearCond;
	}
}


