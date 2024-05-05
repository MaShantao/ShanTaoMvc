package org.springframework.shantaomvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RequestParam注解
 * 用于标识Controller方法的参数，建议http访问参数与方法形参的对应关系
 *
 * @author MashanTao
 * @date 2021/11/26
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {
    // 方法参数名称
    String value() default "";

    // 该参数是否
    boolean required() default true;
}
