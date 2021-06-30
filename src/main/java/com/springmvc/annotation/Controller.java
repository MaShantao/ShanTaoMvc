package com.springmvc.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controller注解
 *
 * @author Ma ShanTao
 * @date 2021/06/27
 */
@Target(value = ElementType.TYPE) // 标识到类型
@Retention(value = RetentionPolicy.RUNTIME) //保留策略
public @interface Controller {
    String value() default "";
}
