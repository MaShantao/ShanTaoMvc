package com.springmvc.annotation;


import java.lang.annotation.*;

/**
 * Service注解
 *
 * @author Ma ShanTao
 * @date 2021/06/27
 */
@Target(value = ElementType.METHOD) // 标识到字段
@Retention(value = RetentionPolicy.RUNTIME) //保留策略
public @interface RequestMapping {
    String value();
}
