package com.newegg.ec.cache.core.mysql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by gl49 on 2018/4/21.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface MysqlField {
    boolean isPrimaryKey() default false;

    boolean notNull() default false;

    String field() default "";

    String type() default "";

    String value() default "";
}
