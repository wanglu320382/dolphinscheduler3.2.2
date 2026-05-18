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

package org.apache.dolphinscheduler.plugin.task.dataxml.util;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ConvertUtil {

    private ConvertUtil() {
    }

    public static Object convertToObject(Object value, Class<?> type) {
        if (value == null) {
            return null;
        }
        if (value instanceof Clob) {
            return convertFromClob((Clob) value);
        }
        if (value instanceof Blob) {
            return convertFromBlob((Blob) value);
        }
        if (type != null && type.toString().equals(value.getClass().toString())) {
            return value;
        }
        return convertByType(value, type);
    }

    private static byte[] convertFromBlob(Blob blob) {
        if (blob == null) {
            return null;
        }
        try {
            return blob.getBytes(1, (int) blob.length());
        } catch (SQLException e) {
            log.debug("Blob类型转换出错");
            return null;
        }
    }

    private static String convertFromClob(Clob clob) {
        if (clob == null) {
            return null;
        }
        try {
            int size = (int) clob.length();
            return clob.getSubString(1, size);
        } catch (SQLException e) {
            log.debug("Clob类型转换出错");
            return null;
        }
    }

    private static Object convertByType(Object value, Class<?> type) {
        if (value == null || type == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return value;
        }
        String strValue = String.valueOf(value);
        try {
            if (String.class.equals(type)) {
                return strValue;
            }
            if (Integer.class.equals(type) || int.class.equals(type)) {
                return Integer.parseInt(strValue);
            }
            if (Long.class.equals(type) || long.class.equals(type)) {
                return Long.parseLong(strValue);
            }
            if (Double.class.equals(type) || double.class.equals(type)) {
                return Double.parseDouble(strValue);
            }
            if (Float.class.equals(type) || float.class.equals(type)) {
                return Float.parseFloat(strValue);
            }
            if (Boolean.class.equals(type) || boolean.class.equals(type)) {
                return Boolean.parseBoolean(strValue);
            }
            if (BigDecimal.class.equals(type)) {
                return new BigDecimal(strValue);
            }
            if (Date.class.equals(type)) {
                if (value instanceof java.util.Date) {
                    return new Date(((java.util.Date) value).getTime());
                }
                return new Date(new SimpleDateFormat("yyyy-MM-dd").parse(strValue).getTime());
            }
        } catch (NumberFormatException | ParseException e) {
            log.debug("类型转换失败，value={}, type={}", value, type.getName());
            return null;
        }
        return value;
    }
}
