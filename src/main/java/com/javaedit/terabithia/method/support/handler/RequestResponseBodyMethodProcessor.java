package com.javaedit.terabithia.method.support.handler;

import com.javaedit.terabithia.method.support.HandlerMethodReturnValueHandler;
import com.javaedit.terabithia.utils.JackSonUtil;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;

import java.nio.charset.StandardCharsets;

/**
 * @author wjw
 * @description: Json类型返回值处理器
 * @title: RequestResponseBodyMethodProcessor
 * @date 2022/6/14 18:17
 */
@Slf4j
public class RequestResponseBodyMethodProcessor implements HandlerMethodReturnValueHandler {

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return true;
    }

    @Override
    public FullHttpResponse handleReturnValue(Object returnValue, MethodParameter returnType, FullHttpRequest request) throws Exception {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

        boolean isCharset = returnValue instanceof CharSequence;
        // 处理返回值
        if (returnValue != null) {
            try {
                String content = null;
                if (isCharset) {
                    content = returnValue.toString();
                    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=UTF-8");
                } else {
                    content = JackSonUtil.toJsonString(returnValue);
                    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
                }
                response.content().writeBytes(content.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                log.error("HandlerMethodReturnValueHandler fail!", e);
                response.setStatus(HttpResponseStatus.SERVICE_UNAVAILABLE);
            }
        }
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }
}
