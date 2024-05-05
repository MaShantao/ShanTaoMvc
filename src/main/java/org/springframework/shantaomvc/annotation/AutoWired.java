package org.springframework.shantaomvc.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AutoWired注解
 *
 * @author Ma ShanTao
 * @date 2021/06/27
 */

@Target(value = ElementType.FIELD) // 标识到字段
@Retention(value = RetentionPolicy.RUNTIME) //保留策略
public @interface AutoWired {
    String value() default "";
}
