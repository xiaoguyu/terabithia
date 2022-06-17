package com.javaedit.terabithia.handler.web;

import com.javaedit.terabithia.method.HandlerMethod;

import java.util.ArrayList;

/**
 * @author wjw
 * @description: 处理器链条
 * @title: HandlerExecutionChain
 * @date 2022/6/14 12:05
 */
public class HandlerExecutionChain {

    private final HandlerMethod handler;

    // 拦截器
//    private final List<HandlerInterceptor> interceptorList = new ArrayList<>();


    public HandlerExecutionChain(HandlerMethod handler) {
        this.handler = handler;
    }

    public HandlerMethod getHandler() {
        return handler;
    }
}
