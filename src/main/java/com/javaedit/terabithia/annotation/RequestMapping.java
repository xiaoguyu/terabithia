package com.javaedit.terabithia.annotation;

import com.javaedit.terabithia.method.annotation.RequestMethod;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author wjw
 * @description: 表示是一个请求方法
 * @title: RequestMapping
 * @date 2022/6/11 11:02
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {

    @AliasFor("path")
    String value() default "";

    @AliasFor("value")
    String path() default "";

    RequestMethod[] method() default {};
}
