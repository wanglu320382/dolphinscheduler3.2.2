/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.plugin.task.dataxml.core;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 参数上下文（移植自 ParamDto）
 */
public class ParamContext extends HashMap<String, Object> {

    private static final long serialVersionUID = 1L;

    public void set(String propName, Object obj) {
        put(propName, obj);
    }

    @SuppressWarnings("unchecked")
    public void set(String propName, int index, Object obj) {
        List<Object> lst = (List<Object>) get(propName);
        if (lst == null) {
            lst = new ArrayList<>();
            put(propName, lst);
        }
        for (int i = lst.size(); i <= index; ++i) {
            lst.add(null);
        }
        lst.set(index, obj);
    }

    public int getRowCount(String propName) {
        Object obj = get(propName);
        if (obj == null) {
            return 0;
        }
        if (obj instanceof List) {
            return ((List<?>) obj).size();
        }
        return -1;
    }

    public Object get(String propName, int index) {
        @SuppressWarnings("unchecked")
        List<Object> lst = (List<Object>) get(propName);
        if (lst == null) {
            return null;
        }
        return lst.get(index);
    }

    public ParamContext getBaseDto(String propName) {
        return convertToBaseDto(get(propName));
    }

    public String getString(String propName) {
        return convertToString(get(propName));
    }

    public String getString(String propName, int index) {
        return convertToString(get(propName, index));
    }

    public BigDecimal getBigDecimal(String propName) {
        return convertToBigDecimal(get(propName));
    }

    public Long getLong(String propName) {
        return convertToLong(get(propName));
    }

    private static ParamContext convertToBaseDto(Object obj) {
        return (ParamContext) obj;
    }

    private static String convertToString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        return obj.toString();
    }

    private static BigDecimal convertToBigDecimal(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        }
        return new BigDecimal(obj.toString());
    }

    private static Long convertToLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Long) {
            return (Long) obj;
        }
        if (obj instanceof Integer) {
            return Long.valueOf(((Integer) obj).intValue());
        }
        return Long.valueOf(obj.toString());
    }

    @SuppressWarnings("unused")
    private static Timestamp convertToTimestamp(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Timestamp) {
            return (Timestamp) obj;
        }
        if (obj instanceof Date) {
            return new Timestamp(((Date) obj).getTime());
        }
        String value = obj.toString();
        SimpleDateFormat sdf;
        switch (value.length()) {
            case 23:
                sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
                break;
            case 19:
                sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                break;
            case 14:
                sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                break;
            case 10:
                sdf = new SimpleDateFormat("yyyy/MM/dd");
                break;
            case 8:
                if (value.indexOf(':') >= 0) {
                    sdf = new SimpleDateFormat("HH:mm:ss");
                } else {
                    sdf = new SimpleDateFormat("yyyyMMdd");
                }
                break;
            default:
                sdf = new SimpleDateFormat();
        }
        try {
            Date dt = sdf.parse(value);
            return new Timestamp(dt.getTime());
        } catch (ParseException e) {
            return null;
        }
    }
}
