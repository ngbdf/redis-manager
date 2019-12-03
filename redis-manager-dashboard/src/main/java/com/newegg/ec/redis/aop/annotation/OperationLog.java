package com.newegg.ec.redis.aop.annotation;

import com.newegg.ec.redis.entity.OperationObjectType;
import com.newegg.ec.redis.entity.OperationType;

import javax.validation.constraints.NotNull;
import java.lang.annotation.*;

/**
 * @author fw13
 * @date 2019/11/20 11:17
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 用户操作类型
     * @return
     */
    @NotNull
    OperationType type();

    /**
     * 用户操作对象
     * @return
     */
    @NotNull
    OperationObjectType objType();

}
