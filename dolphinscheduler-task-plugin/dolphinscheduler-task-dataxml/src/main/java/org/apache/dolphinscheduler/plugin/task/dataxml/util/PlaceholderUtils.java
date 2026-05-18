package org.apache.dolphinscheduler.plugin.task.dataxml.util;

import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.dataxml.core.ParamContext;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * 占位符与表达式工具。
 */
public final class PlaceholderUtils {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("#\\{\\s*[\\w.]+\\s*}");
    private static final Pattern WHOLE_PLACEHOLDER_PATTERN = Pattern.compile("^#\\{\\s*([\\w.]+)\\s*}$");
    private static final SpelExpressionParser SPEL_PARSER = new SpelExpressionParser();

    private PlaceholderUtils() {
    }

    public static String replace(String text, ParamContext param, boolean strict) throws TaskException {
        if (StringUtils.isEmpty(text)) {
            return text;
        }
        Matcher m = PLACEHOLDER_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String key = m.group().replaceAll("#|\\{|}|\\s", "");
            Object value = resolvePlaceholderValue(param, key);
            if (value != null) {
                String val = String.valueOf(value);
                m.appendReplacement(sb, Matcher.quoteReplacement(val != null ? val : ""));
            } else if (strict) {
                throw new TaskException("参数中缺少占位符对应键：" + key);
            } else {
                m.appendReplacement(sb, Matcher.quoteReplacement(m.group()));
            }
        }
        m.appendTail(sb);
        if (sb.length() > 0) {
            return sb.toString();
        }
        return text;
    }

    @SuppressWarnings("unchecked")
    private static Object resolvePlaceholderValue(ParamContext param, String key) {
        if (param.containsKey(key)) {
            return param.get(key);
        }
        if (!key.contains(".")) {
            return null;
        }
        String[] parts = key.split("\\.");
        Object cur = param.get(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (cur == null) {
                return null;
            }
            if (cur instanceof ParamContext) {
                cur = ((ParamContext) cur).get(parts[i]);
                continue;
            }
            if (cur instanceof java.util.Map) {
                cur = ((java.util.Map<String, Object>) cur).get(parts[i]);
                continue;
            }
            return null;
        }
        return cur;
    }

    public static String extractWholePlaceholderPath(String text) {
        if (StringUtils.isEmpty(text)) {
            return null;
        }
        Matcher m = WHOLE_PLACEHOLDER_PATTERN.matcher(text.trim());
        if (!m.matches()) {
            return null;
        }
        return m.group(1);
    }

    public static Object getParamValueByPath(ParamContext param, String dottedPath) {
        if (param == null || StringUtils.isEmpty(dottedPath)) {
            return null;
        }
        return resolvePlaceholderValue(param, dottedPath.trim());
    }

    public static boolean shouldExecuteByExpressionCond(String templateExpressionCond, ParamContext param)
            throws TaskException {
        if (StringUtils.isEmpty(templateExpressionCond)) {
            return true;
        }
        String replaced = replace(templateExpressionCond, param, true);
        return evaluateSpelBoolean(replaced);
    }

    public static boolean evaluateSpelBoolean(String spel) throws TaskException {
        if (StringUtils.isEmpty(spel)) {
            return true;
        }
        try {
            StandardEvaluationContext ctx = new StandardEvaluationContext();
            Expression ex = SPEL_PARSER.parseExpression(spel);
            Object v = ex.getValue(ctx);
            if (v instanceof Boolean) {
                return (Boolean) v;
            }
            if (v instanceof Number) {
                return ((Number) v).doubleValue() != 0.0d;
            }
            return v != null;
        } catch (Exception e) {
            throw new TaskException("expressionCond SpEL 求值失败：" + localizedOrMessage(e) + "；表达式：" + spel);
        }
    }

    public static void appendTableMsg(ParamContext param, String toObjectName, int total) {
        String oldTableMsg = param.getString("tableMsg");
        if (StringUtils.isEmpty(oldTableMsg)) {
            param.put("tableMsg", toObjectName + ": " + total);
            return;
        }
        param.put("tableMsg", oldTableMsg + ';' + toObjectName + ": " + total);
    }

    public static Class<?> resolveClass(String className) throws TaskException {
        String targetClassName = trim(className);
        if (StringUtils.isEmpty(targetClassName)) {
            throw new TaskException("类名不能为空");
        }
        ClassLoader[] classLoaders = new ClassLoader[]{
                Thread.currentThread().getContextClassLoader(),
                PlaceholderUtils.class.getClassLoader(),
                ClassLoader.getSystemClassLoader()
        };
        for (ClassLoader classLoader : classLoaders) {
            if (classLoader == null) {
                continue;
            }
            try {
                return Class.forName(targetClassName, true, classLoader);
            } catch (ClassNotFoundException ignored) {
                // try next
            }
        }
        throw new TaskException("找不到类：" + targetClassName);
    }

    public static String localizedOrMessage(Throwable e) {
        String m = e.getLocalizedMessage();
        if (StringUtils.isEmpty(m)) {
            m = e.getMessage();
        }
        return m != null ? m : "";
    }

    public static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
