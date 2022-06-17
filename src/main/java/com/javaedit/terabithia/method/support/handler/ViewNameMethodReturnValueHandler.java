package com.javaedit.terabithia.method.support.handler;

import com.javaedit.terabithia.method.support.HandlerMethodReturnValueHandler;
import io.netty.handler.codec.http.*;
import org.springframework.core.MethodParameter;

import java.nio.charset.StandardCharsets;

/**
 * @author wjw
 * @description: 返回值处理器-处理void和字符串类型
 * @title: ViewNameMethodReturnValueHandler
 * @date 2022/6/17 14:52
 */
public class ViewNameMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        Class<?> paramType = returnType.getParameterType();
        return (void.class == paramType || CharSequence.class.isAssignableFrom(paramType));
    }

    @Override
    public FullHttpResponse handleReturnValue(Object returnValue, MethodParameter returnType, FullHttpRequest webRequest) throws Exception {
        String content = null;
        if (returnValue instanceof CharSequence) {
            content = returnValue.toString();
        } else if (returnValue != null) {
            // should not happen
            throw new UnsupportedOperationException("Unexpected return type: " +
                    returnType.getParameterType().getName() + " in method: " + returnType.getMethod());
        } else {
            content = "";
        }
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.content().writeBytes(content.getBytes(StandardCharsets.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=UTF-8");
        return response;
    }
}
