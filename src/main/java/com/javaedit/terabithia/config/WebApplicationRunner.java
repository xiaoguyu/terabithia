package com.javaedit.terabithia.config;

import com.javaedit.terabithia.handler.web.HandlerInterceptor;
import com.javaedit.terabithia.method.annotation.RequestMappingHandlerMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author wjw
 * @description: 用于对springboot初始化完成后的一些处理
 * @title: WebApplicationRunner
 * @date 2022/6/17 11:43
 */
@Component
public class WebApplicationRunner implements ApplicationRunner {

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;
    @Autowired(required = false)
    private List<HandlerInterceptor> interceptorList;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        /**
         * 注入拦截器
         */
        // 考虑排序
        // TODO
        if (null != interceptorList) {
            for (HandlerInterceptor handlerInterceptor : interceptorList) {
                requestMappingHandlerMapping.addInterceptor(handlerInterceptor);
            }
        }
    }
}
