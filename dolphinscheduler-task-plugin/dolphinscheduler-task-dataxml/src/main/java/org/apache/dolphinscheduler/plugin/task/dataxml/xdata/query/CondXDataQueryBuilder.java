package org.apache.dolphinscheduler.plugin.task.dataxml.xdata.query;

import org.apache.dolphinscheduler.plugin.task.dataxml.core.ParamContext;

import org.apache.commons.lang3.StringUtils;

import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.XdataTemplate;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
/**
 * 条件模式查询构建器。
 * <p>支持将 {@code queryCond}/{@code clearCond} 中的占位符替换为运行参数。</p>
 *
 * @author wanglu
 * @date 2026-04-23
 */
public class CondXDataQueryBuilder extends XDataQueryBuilder {
	private static CondXDataQueryBuilder instance = null;
	private Pattern pattern = Pattern.compile("#\\{ *\\w+ *\\}");

	/**
	 * 私有构造，限制外部直接实例化。
	 */
	private CondXDataQueryBuilder() {
	}

	/**
	 * 获取条件查询构建器单例。
	 *
	 * @return 条件查询构建器
	 */
	public static CondXDataQueryBuilder getInstance() {
		if (instance == null) {
			instance = new CondXDataQueryBuilder();
		}
		return instance;
	}

	/**
	 * 生成条件抓取 SQL 的 where 条件。
	 *
	 * @param template xdata 模板
	 * @param param 运行参数
	 * @return 查询条件
	 */
	@Override
	public String genQueryCondition(XdataTemplate template, ParamContext param) throws TaskException {
		String queryCond = template.getQueryCond();
		if (!StringUtils.isEmpty(queryCond)) {
			Matcher m = pattern.matcher(queryCond);
			StringBuffer sb = new StringBuffer();
			while (m.find()) {
				String key = m.group().replaceAll("#|\\{|\\}| ", "");		
				if (param.containsKey(key)) {
					m.appendReplacement(sb, param.getString(key));
				} else {
					throw new TaskException(template.getFromObjectName() + "表导出条件" + queryCond
							+ "中指定条件参数(" + key + ")无法取得");
				}
			}
			if(m!= null){
				m.appendTail(sb);
			}
			if (sb.length() > 0) {
				queryCond = sb.toString();
			}
		}
		return queryCond;
	}

	/**
	 * 生成条件模式下的清理条件。
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
			clearCond = template.getQueryCond();
		}

		if (!StringUtils.isEmpty(clearCond)) {
			Matcher m = pattern.matcher(clearCond);
			StringBuffer sb = new StringBuffer();
			while (m.find()) {
				String key = m.group().replaceAll("#|\\{|\\}| ", "");
				if (param.containsKey(key)) {
					m.appendReplacement(sb, param.getString(key));
				} else {
					throw new TaskException(template.getFromObjectName() + "表导出条件" + clearCond
							+ "中指定条件参数(" + key + ")无法取得");
				}
			}
			if(m!= null){
				m.appendTail(sb);
			}
			if (sb.length() > 0) {
				clearCond = sb.toString();
			}
		}
		return clearCond;
	}

	@Override
	public String genRollBackSQL(XdataTemplate template, ParamContext param) {
		return null;
	}

}


