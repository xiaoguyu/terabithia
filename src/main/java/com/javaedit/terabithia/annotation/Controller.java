package com.javaedit.terabithia.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author wjw
 * @description: 表示是一个controller类
 * @title: Controller
 * @date 2022/6/11 10:47
 */
@Target({ElementType.TYPE}) // 修饰的对象范围
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Controller {

    @AliasFor(annotation = Component.class)
    String value() default "";

}
