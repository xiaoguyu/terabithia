package com.javaedit.terabithia.method.annotation;

import com.javaedit.terabithia.method.HandlerMethod;
import com.javaedit.terabithia.method.support.handler.HandlerMethodReturnValueHandlerComposite;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * Extension of {@link HandlerMethod} that invokes the underlying method with
 * argument values resolved from the current HTTP request through a list of
 * {@link HandlerMethodArgumentResolver}.
 *
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @author Sebastien Deleuze
 * @description: HandlerMethod的扩展，增加了解析参数和调用的方法
 * @update wjw 2022/6/14 16:34
 * @since 3.1
 */
public class InvocableHandlerMethod extends HandlerMethod {

    private static final Object[] EMPTY_ARGS = new Object[0];
//    private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Nullable
    private HandlerMethodReturnValueHandlerComposite returnValueHandlers;

    public InvocableHandlerMethod(HandlerMethod handlerMethod) {
        super(handlerMethod);
    }


    public FullHttpResponse invokeAndHandle(FullHttpRequest request) throws Exception {
        // 参数封装处理
        Object[] args = getMethodArgumentValues(request);
        // 调用方法
        Object returnValue = doInvoke(args);
        // 处理返回值
        return this.returnValueHandlers.handleReturnValue(returnValue, getReturnValueType(returnValue), request);
    }

    private Object doInvoke(Object... args) throws Exception {
        Method method = getBridgedMethod();
        // 将方法设置为可调用
        ReflectionUtils.makeAccessible(method);

        return method.invoke(getBean(), args);
    }

    protected Object[] getMethodArgumentValues(FullHttpRequest request) throws Exception {
        MethodParameter[] parameters = getMethodParameters();
        // 如果没有参数
        if (ObjectUtils.isEmpty(parameters)) {
            return EMPTY_ARGS;
        }

        // 参数解析器下个版本再整
        Object[] args = new Object[1];
        args[0] = request;


//        Object[] args = new Object[parameters.length];
//        for (int i = 0; i < parameters.length; i++) {
//            MethodParameter parameter = parameters[i];
//            // 名称解析器
//            parameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
//
//        }

        return args;
    }

    public void setHandlerMethodReturnValueHandlers(HandlerMethodReturnValueHandlerComposite returnValueHandlers) {
        this.returnValueHandlers = returnValueHandlers;
    }
}
