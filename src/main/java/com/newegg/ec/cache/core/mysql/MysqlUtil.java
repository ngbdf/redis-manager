package com.newegg.ec.cache.core.mysql;

import com.newegg.ec.cache.app.util.ClassUtil;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by gl49 on 2018/4/21.
 */
public class MysqlUtil {
    public static List<String> initMysqlTable(List<String> packages) {
        Set<Class<?>> classSets = new HashSet<>();
        for (String p : packages) {
            Set<Class<?>> classSet = ClassUtil.getClasses(p);
            classSets.addAll(classSet);
        }
        List<String> sqlList = new ArrayList<>();
        for (Class<?> c : classSets) {
            String sql = autoCreateMysqlTable(c);
            if (!StringUtils.isBlank(sql)) {
                sqlList.add(sql);
            }
        }
        return sqlList;
    }

    private static String autoCreateMysqlTable(Class claz) {
        String createTable = null;
        try {
            Annotation[] annotations = claz.getAnnotations();
            if (annotations.length > 0) {
                for (Annotation annotation : annotations) {
                    if (annotation instanceof MysqlTable) {
                        MysqlTable mysqlTable = (MysqlTable) annotation;
                        if (!mysqlTable.autoCreate()) {
                            break;
                        }
                        String tableName = mysqlTable.name();
                        createTable = createTableSql(claz, mysqlTable, tableName);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return createTable;
    }

    public static String createTableSql(Class claz, String tableName) {
        MysqlTable mysqlTable = (MysqlTable) claz.getAnnotation(MysqlTable.class);
        return createTableSql(claz, mysqlTable, tableName);
    }

    public static String createTableSql(Class claz, MysqlTable mysqlTable, String tableName) {
        String createTableStr = "create table if not exists %s(%s)engine=%s DEFAULT CHARSET=%s";
        String engine = mysqlTable.engine();
        String charset = mysqlTable.charset();
        List<String> fieldList = new ArrayList<>();
        Field[] fieldsDecrs = claz.getDeclaredFields();
        for (Field field : fieldsDecrs) {
            MysqlField fieldMeta = field.getAnnotation(MysqlField.class);
            if (null == fieldMeta) {
                continue;
            }
            String fieldName = fieldMeta.field();
            if (StringUtils.isBlank(fieldName)) {
                fieldName = field.getName();
            }
            String baseStr = fieldName + " " + fieldMeta.type();
            if (fieldMeta.isPrimaryKey()) {
                fieldList.add(baseStr + " auto_increment primary key");
            } else if (fieldMeta.notNull()) {
                fieldList.add(baseStr + " not null");
            } else {
                fieldList.add(baseStr);
            }
        }
        String fieldStr = StringUtils.join(fieldList, ",");
        String createTable = String.format(createTableStr, tableName, fieldStr, engine, charset);
        return createTable;
    }

}
