package com.javaedit.terabithia.handler.web;

import com.javaedit.terabithia.method.HandlerMethod;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.springframework.lang.Nullable;

/**
 * @author wjw
 * @description: 拦截器
 * @title: HandlerInterceptor
 * @date 2022/6/17 10:50
 */
public interface HandlerInterceptor {
    
    /**
     * @apiNote 过滤器是否适用当前请求
     * @param request
     * @return
     * @author wjw
     * @date 2022/6/17 14:00
     */
    default boolean match(FullHttpRequest request) {
        return true;
    }

    /**
     * @param ctx
     * @param request
     * @param handler
     * @return 返回false则不会执行handler
     * @apiNote handler方法之前执行
     * @author wjw
     * @date 2022/6/17 10:52
     */
    default boolean preHandle(ChannelHandlerContext ctx, FullHttpRequest request, HandlerMethod handler) throws Exception {

        return true;
    }

    /**
     * @param ctx
     * @param request
     * @param response
     * @param handler
     * @return
     * @apiNote handler方法之后执行
     * @author wjw
     * @date 2022/6/17 10:53
     */
    default void postHandle(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response, HandlerMethod handler) throws Exception {
    }

    /**
     * @param ctx
     * @param request
     * @param handler
     * @param ex
     * @return
     * @apiNote 拦截器执行完成后
     * @author wjw
     * @date 2022/6/17 10:58
     */
    default void afterCompletion(ChannelHandlerContext ctx, FullHttpRequest request, Object handler,
                                 @Nullable Exception ex) throws Exception {
    }
}
