package com.springmvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.METHOD, ElementType.ANNOTATION_TYPE}) // 标识到方法或者类
@Retention(value = RetentionPolicy.RUNTIME) //保留策略
public @interface ResponseBody {
}
