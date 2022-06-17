package com.javaedit.terabithia.method.support;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.springframework.core.MethodParameter;

/**
 * Strategy interface to handle the value returned from the invocation of a
 * handler method .
 *
 * @author Arjen Poutsma
 * @see HandlerMethodArgumentResolver
 * @since 3.1
 */
public interface HandlerMethodReturnValueHandler {

    /**
     * @param returnType
     * @return
     * @apiNote 判断是否支持该方法
     * @author wjw
     * @date 2022/6/15 10:35
     */
    boolean supportsReturnType(MethodParameter returnType);

    /**
     * @param returnValue
     * @param returnType
     * @param webRequest
     * @return
     * @apiNote 处理返回值
     * @author wjw
     * @date 2022/6/15 10:36
     */
    FullHttpResponse handleReturnValue(Object returnValue, MethodParameter returnType, FullHttpRequest webRequest) throws Exception;


}
