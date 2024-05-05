package org.springframework.shantaomvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ResponseBody注解
 *
 * @author MashanTao
 * @date 2021/11/26
 */
@Target(value = {ElementType.METHOD, ElementType.ANNOTATION_TYPE}) // 标识到方法或者类
@Retention(value = RetentionPolicy.RUNTIME) //保留策略
public @interface ResponseBody {

}
