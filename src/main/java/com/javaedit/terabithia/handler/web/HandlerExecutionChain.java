package com.javaedit.terabithia.handler.web;

import com.javaedit.terabithia.method.HandlerMethod;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wjw
 * @description: 处理器链条
 * @title: HandlerExecutionChain
 * @date 2022/6/14 12:05
 */
@Slf4j
public class HandlerExecutionChain {

    private final HandlerMethod handler;

    // 拦截器
    private final List<HandlerInterceptor> interceptorList = new ArrayList<>();

    /**
     * 拦截器下标（标识使用到了哪一个拦截器）
     */
    private int interceptorIndex = -1;


    public HandlerExecutionChain(HandlerMethod handler) {
        this.handler = handler;
    }

    public HandlerMethod getHandler() {
        return handler;
    }

    /**
     * @param ctx
     * @param request
     * @return
     * @apiNote 执行前置拦截器方法
     * @author wjw
     * @date 2022/6/17 10:59
     */
    public boolean applyPreHandle(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        for (int i = 0; i < this.interceptorList.size(); i++) {
            HandlerInterceptor interceptor = this.interceptorList.get(i);
            if (!interceptor.preHandle(ctx, request, this.handler)) {
                triggerAfterCompletion(ctx, request, null);
                return false;
            }
            this.interceptorIndex = i;
        }
        return true;
    }

    /**
     * @param ctx
     * @param request
     * @param ex
     * @return
     * @apiNote 触发拦截器完成方法
     * @author wjw
     * @date 2022/6/17 10:59
     */
    public void triggerAfterCompletion(ChannelHandlerContext ctx, FullHttpRequest request, @Nullable Exception ex) {
        for (int i = this.interceptorIndex; i >= 0; i--) {
            HandlerInterceptor interceptor = this.interceptorList.get(i);
            try {
                interceptor.afterCompletion(ctx, request, this.handler, ex);
            } catch (Throwable ex2) {
                log.error("HandlerInterceptor.afterCompletion threw exception", ex2);
            }
        }
    }

    public void applyPostHandle(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) throws Exception {
        for (int i = this.interceptorList.size() - 1; i >= 0; i--) {
            HandlerInterceptor interceptor = this.interceptorList.get(i);
            interceptor.postHandle(ctx, request, response, this.handler);
        }
    }

    /**
     * @param interceptor
     * @return
     * @apiNote 添加拦截器
     * @author wjw
     * @date 2022/6/17 11:33
     */
    public void addInterceptor(HandlerInterceptor interceptor) {
        this.interceptorList.add(interceptor);
    }
}
