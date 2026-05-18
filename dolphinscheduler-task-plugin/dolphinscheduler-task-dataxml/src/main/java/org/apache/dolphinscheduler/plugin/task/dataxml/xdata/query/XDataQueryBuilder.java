package org.apache.dolphinscheduler.plugin.task.dataxml.xdata.query;

import org.apache.dolphinscheduler.plugin.task.dataxml.core.ParamContext;

import org.apache.commons.lang3.StringUtils;

import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.ClearOldDataType;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.Column;
import org.apache.dolphinscheduler.plugin.task.dataxml.xdata.template.XdataTemplate;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
/**
 * XData 组件查询构建器抽象基类。
 *
 * @author wanglu
 * @date 2026-04-23
 */
public abstract class XDataQueryBuilder {

	private Pattern patternCh = Pattern.compile("#\\{[\\p{IsHan}]+\\}");

	private Pattern pattern = Pattern.compile("#\\{ *\\w+ *\\}");

	/**
	 * 源头表SELECT字段列标志
	 */
	public final static String COLUMN_FROM_TABLE = "FROM_TABLE";
	
	/**
	 * 目标表SELECT字段列标志
	 */
	public final static String COLUMN_TO_TABLE = "TO_TABLE";
	
	/**
	 * 源头表INSERT字段列标志
	 */
	public final static String COLUMN_INSERT = "INSERT_VAL";

	/**
	 * 源头表UPDATE字段列标志
	 */
	public final static String COLUMN_UPDATE = "COLUMN_UPDATE";
	/**
	 * 源头表UPDATE字段列标志
	 */
	public final static String COLUMN_UPDATE_COND = "COLUMN_UPDATE_COND";

	/**
	 * 源头表INSERT字段列标志
	 */
	public final static String FORM_TYPE_COND = "cond";
	
	/**
	 * 生成导入数据过滤条件（全量/增量/条件模式由子类实现）。
	 *
	 * @param template XData 模板
	 * @param param 运行参数
	 * @return where 条件（不含 where 关键字）
	 * @throws TaskException 生成条件失败
	 */
	protected abstract String genQueryCondition(XdataTemplate template, ParamContext param) throws TaskException;

	/**
	 * 生成清理旧数据过滤条件（全量/增量/条件模式由子类实现）。
	 *
	 * @param template XData 模板
	 * @param param 运行参数
	 * @return 清理条件（不含 where 关键字）
	 * @throws TaskException 生成条件失败
	 */
	protected abstract String genClearCondition(XdataTemplate template, ParamContext param) throws TaskException;

	/**
	 * 生成查询来源表表达式，支持读取外部 SQL 模板。
	 *
	 * @param template xdata 模板
	 * @param param 运行参数
	 * @return 查询表或子查询表达式
	 */
	public String genQueryTable(XdataTemplate template, ParamContext param) {
		String querytable = template.getFromObjectName();
		if (!StringUtils.isEmpty(querytable)) {
			Matcher m = patternCh.matcher(querytable);
			StringBuffer sb = new StringBuffer();
			while (m.find()) {
				String key = m.group().replaceAll("#|\\{|\\}| ", "");
				Path fullPath = Paths.get(param.getString("path")).resolve(param.getString("job")).resolve(key+".sql");
				querytable = " (" + readSQLFile(fullPath.toString()) + ") A ";
			}
		}
		return querytable;
	}

	/**
	 * 读取 SQL 文件并过滤注释行。
	 *
	 * @param filePath SQL 文件路径
	 * @return SQL 文本
	 */
	private String readSQLFile(String filePath) {
		StringBuffer execSql = new StringBuffer();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));

			String line = "";
			// 逐行读取文件内容并打印到控制台
			while ((line = reader.readLine()) != null) {
				if (!line.trim().startsWith("--")) {
					execSql.append(line);
				}
			}
			// 关闭文件读取器
			reader.close();

		} catch (IOException e) {
			log.error("读取SQL文件时出现错误: {}", e.getLocalizedMessage(), e);
		}
		return execSql.toString();
	}
	
	/**
	 * 生成完整查询 SQL。
	 *
	 * @param template xdata 模板
	 * @param param 运行参数
	 * @return 查询 SQL
	 */
	public String genQuerySQL(XdataTemplate template, ParamContext param) throws TaskException {

		String queryCond = genQueryCondition(template, param);

		String querySQL = "select "
				+ getColumnNames(template, COLUMN_FROM_TABLE)
				+ " from "
				+ CondXDataQueryBuilder.getInstance().genQueryTable(template, param)
				+ (StringUtils.isEmpty(queryCond) ? "" : " where "
						+ queryCond);

		return convQuerySQL(querySQL,param);
	}

	/**
	 * 获取并替换表达式条件中的占位参数。
	 *
	 * @param template xdata 模板
	 * @param param 运行参数
	 * @return 表达式条件
	 */
	public String getExpressionCond(XdataTemplate template, ParamContext param) {

		String expressionCond = template.getExpressionCond();

		return convQuerySQL(expressionCond,param);
	}

	/**
	 * 将 SQL 中的占位参数替换为运行参数值。
	 *
	 * @param querySQL 原始 SQL
	 * @param param 运行参数
	 * @return 替换后的 SQL
	 */
	public String convQuerySQL(String querySQL, ParamContext param) {
		if (!StringUtils.isEmpty(querySQL)) {
			Matcher m = pattern.matcher(querySQL);
			StringBuffer sb = new StringBuffer();
			while (m.find()) {
				String key = m.group().replaceAll("#|\\{|\\}| ", "");
				if (param.containsKey(key)) {
					m.appendReplacement(sb, param.getString(key));
				}
			}
			if(m!= null){
				m.appendTail(sb);
			}
			if (sb.length() > 0) {
				querySQL = sb.toString();
			}
		}
		return querySQL;
	}
	
	/**
	 * 生成清理旧数据 SQL 语句。
	 * <ul>
	 * <li>条件模式：{@code delete from 表 where 条件}</li>
	 * <li>全量模式：{@code truncate table 表}</li>
	 * </ul>
	 *
	 * @param template XData 模板
	 * @param param 运行参数
	 * @return 清理 SQL；无需清理时返回 null
	 * @throws TaskException 生成 SQL 失败
	 */
	public String genClearOldDataSQL(XdataTemplate template, ParamContext param) throws TaskException {
		
		String deleteSql = null;
		
		if (template.isClearOldData()
				|| template.getClearOldDataType() == ClearOldDataType.ALL) {
			deleteSql = "truncate table " + template.getToObjectName();

		} else if (template.getClearOldDataType() == ClearOldDataType.CONDITION) {
			String queryCond = genQueryCondition(template, param);
			if (StringUtils.isEmpty(queryCond)) {
				deleteSql = "truncate table " + template.getToObjectName();
			} else {
				deleteSql = "delete from " + template.getToObjectName()
						+ " where " + queryCond;			
			}
		}
		return deleteSql;
	}

	/**
	 * 按 clearCond / queryCond 生成清理旧数据 SQL。
	 * <ul>
	 * <li>条件模式：{@code delete from 表 where 条件}</li>
	 * <li>全量模式：{@code truncate table 表}</li>
	 * </ul>
	 *
	 * @param template XData 模板
	 * @param param 运行参数
	 * @return 清理 SQL；无需清理时返回 null
	 * @throws TaskException 生成 SQL 失败
	 */
	public String genClearOldDataSQLByCond(XdataTemplate template, ParamContext param) throws TaskException {

		String deleteSql = null;

		if (template.isClearOldData()
				|| template.getClearOldDataType() == ClearOldDataType.ALL) {
			deleteSql = "truncate table " + template.getToObjectName();

		} else if (template.getClearOldDataType() == ClearOldDataType.CONDITION) {
			String clearCond = CondXDataQueryBuilder.getInstance().genClearCondition(template, param);
			if (StringUtils.isEmpty(clearCond)) {
				deleteSql = "truncate table " + template.getToObjectName();
			} else {
				deleteSql = "delete from " + template.getToObjectName()
						+ " where " + clearCond;
			}
		}
		return deleteSql;
	}
	
	/**
	 * 生成目标表 INSERT SQL。
	 *
	 * @param template XData 模板
	 * @param param 运行参数
	 * @return INSERT SQL
	 * @throws TaskException 目标表无法确定时抛出
	 */
	public String genInsertSQL(XdataTemplate template, ParamContext param) throws TaskException {
		String toObjectName = template.getToObjectName() != null ? template
				.getToObjectName() : template.getFromObjectName();

		if (StringUtils.isEmpty(toObjectName)) {
			throw new TaskException("导入配置文件有误，无法确定导入目标表");
		}

		return "insert into " + toObjectName + "("
				+ getColumnNames(template, COLUMN_TO_TABLE) + ") values("
				+ getColumnNames(template, COLUMN_INSERT) + ")";
	}

	/**
	 * 生成目标表 UPDATE SQL。
	 *
	 * @param template XData 模板
	 * @param param 运行参数
	 * @return UPDATE SQL
	 * @throws TaskException 目标表无法确定时抛出
	 */
	public String genUpdateSQL(XdataTemplate template, ParamContext param) throws TaskException {
		String toObjectName = template.getToObjectName() != null ? template
				.getToObjectName() : template.getFromObjectName();

		if (StringUtils.isEmpty(toObjectName)) {
			throw new TaskException("导入配置文件有误，无法确定导入目标表！");
		}

		return "update " + toObjectName + " set "
				+ getUpdateCond(template, COLUMN_UPDATE) + " where "
				+ getUpdateCond(template, COLUMN_UPDATE_COND);
	}

	/**
	 * 获取回滚 SQL。
	 *
	 * @param template XData 模板
	 * @param param 运行参数
	 * @return 回滚 SQL；无回滚策略可返回空串
	 * @throws TaskException 生成失败
	 */
	public abstract String genRollBackSQL(XdataTemplate template, ParamContext param) throws TaskException;

	/**
	 * 生成 UPDATE 语句的 set / where 字段片段。
	 *
	 * @param template XData 模板
	 * @param flag 字段片段类型
	 * @return SQL 片段
	 */
	public static String getUpdateCond(XdataTemplate template, String flag){

		List<Column> columns = template.getColumn();
		StringBuffer sb = new StringBuffer();

		if(COLUMN_UPDATE.equals(flag)){
			for(Column column :columns){
				if(column.getToName()!= null && !FORM_TYPE_COND.equals(column.getFromType())){
					sb.append(column.getToName()).append("= ? ,");
				}
			}
			if(sb.length() > 1){
				return sb.substring(0, sb.length() - 2);
			}else{
				return sb.toString();
			}
		} else if(COLUMN_UPDATE_COND.equals(flag)){
			for(Column column :columns){
				if(column.getToName()!= null && FORM_TYPE_COND.equals(column.getFromType())){
					sb.append(column.getToName()).append(column.getCondType()).append("? and ");
				}
			}
			if(sb.length() > 1){
				return sb.substring(0, sb.length() - 5);
			}else{
				return sb.toString();
			}
		}
		return "";
	}
	
	/**
	 * 生成列名片段或占位符片段。
	 *
	 * @param template XData 模板
	 * @param flag 片段类型标识
	 * @return SQL 片段
	 */
	public static String getColumnNames(XdataTemplate template, String flag){
		
		List<Column> columns = template.getColumn();
		StringBuffer sb = new StringBuffer();
		
		if(COLUMN_FROM_TABLE.equals(flag)){
			// 源表SELECT语句
			for(Column column :columns){
				if(!StringUtils.isEmpty(column.getFromSql())){
					sb.append("(").append(column.getFromSql()).append(") ").append(column.getFromName()).append(", ");
				}else{
					sb.append(column.getFromName()).append(", ");
				}
			}
		}else if(COLUMN_TO_TABLE.equals(flag)){
			// 目标表SELECT语句
			for(Column column :columns){
				if(column.getToName()!= null){
					sb.append(column.getToName()).append(", ");
				}else{
					sb.append(column.getFromName()).append(", ");
				}
			}
		}else if(COLUMN_INSERT.equals(flag)){
			// INSERT语句
			for(int i = 0; i < columns.size(); i++){
				sb.append("?").append(", ");
			}
		}else{
			sb.append("*, ");
		}

		if(sb.length() > 1){
			return sb.substring(0, sb.length() - 2); 
		}else{
			return sb.toString();
		}
		
	}

	/**
	 * 获取序列字段（seqColumn=true 的列）。
	 *
	 * @param columns 列配置
	 * @return 序列列；未配置时返回 null
	 */
	protected Column getSeqColumn(List<Column> columns){
		
		for(Column column :columns){
			if(column.isSeqColumn()){
				return column;
			}
		}
		return null;
	}


}


