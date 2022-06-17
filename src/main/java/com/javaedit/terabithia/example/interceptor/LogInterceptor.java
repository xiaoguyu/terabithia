package com.javaedit.terabithia.example.interceptor;

import com.javaedit.terabithia.handler.web.HandlerInterceptor;
import com.javaedit.terabithia.method.HandlerMethod;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author wjw
 * @description: 日志拦截器
 * @title: LogInterceptor
 * @date 2022/6/17 11:55
 */
@Slf4j
@Component
public class LogInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(ChannelHandlerContext ctx, FullHttpRequest request, HandlerMethod handler) throws Exception {
        System.out.println("LogInterceptor: request uri:" + request.uri());
        return true;
    }

    @Override
    public void afterCompletion(ChannelHandlerContext ctx, FullHttpRequest request, Object handler, Exception ex) throws Exception {
        if (null != ex) {
            System.out.println("出异常了:" + ex.getMessage());
        }
    }
}
