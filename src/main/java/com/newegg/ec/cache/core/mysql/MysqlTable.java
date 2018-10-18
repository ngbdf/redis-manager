package com.newegg.ec.cache.core.mysql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by gl49 on 2018/4/21.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface MysqlTable {
    String name();

    String engine() default "innodb";

    String charset() default "utf8";

    boolean autoCreate() default true;
}
