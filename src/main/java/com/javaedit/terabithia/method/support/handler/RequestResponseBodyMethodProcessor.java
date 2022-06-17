package com.javaedit.terabithia.method.support.handler;

import com.javaedit.terabithia.annotation.ResponseBody;
import com.javaedit.terabithia.method.support.HandlerMethodReturnValueHandler;
import com.javaedit.terabithia.utils.JackSonUtil;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.nio.charset.StandardCharsets;

/**
 * @author wjw
 * @description: 返回值处理器-处理@ResponseBody注解
 * @title: RequestResponseBodyMethodProcessor
 * @date 2022/6/14 18:17
 */
@Slf4j
public class RequestResponseBodyMethodProcessor implements HandlerMethodReturnValueHandler {

    /**
     * @apiNote 判断类或者方法上有@ResponseBody注解
     */
    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return (AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), ResponseBody.class) ||
                returnType.hasMethodAnnotation(ResponseBody.class));
    }

    @Override
    public FullHttpResponse handleReturnValue(Object value, MethodParameter returnType, FullHttpRequest request) throws Exception {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        String content = null;

        if (value instanceof CharSequence) {
            content = value.toString();
        } else if (null != value) {
            content = JackSonUtil.toJsonString(value);
        } else {
            content = "";
        }
        response.content().writeBytes(content.getBytes(StandardCharsets.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");

        return response;
    }

}
