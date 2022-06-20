package com.javaedit.terabithia.method.support.handler;

import com.javaedit.terabithia.method.support.HandlerMethodArgumentResolver;
import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.core.MethodParameter;

/**
 * @author wjw
 * @description: netty内置http对象请求处理器
 * @title: NettyRequestMethodArgumentResolver
 * @date 2022/6/20 11:44
 */
public class NettyRequestMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> paramType = parameter.getParameterType();
        return FullHttpRequest.class.isAssignableFrom(paramType);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, FullHttpRequest webRequest) throws Exception {
        return webRequest;
    }
}
