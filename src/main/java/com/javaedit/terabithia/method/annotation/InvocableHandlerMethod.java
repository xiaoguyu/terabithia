package com.javaedit.terabithia.method.annotation;

import com.javaedit.terabithia.method.HandlerMethod;
import com.javaedit.terabithia.method.support.HandlerMethodArgumentResolver;
import com.javaedit.terabithia.method.support.handler.HandlerMethodArgumentResolverComposite;
import com.javaedit.terabithia.method.support.handler.HandlerMethodReturnValueHandlerComposite;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

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
@Slf4j
public class InvocableHandlerMethod extends HandlerMethod {

    private static final Object[] EMPTY_ARGS = new Object[0];

    private HandlerMethodArgumentResolverComposite resolvers = new HandlerMethodArgumentResolverComposite();

    private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
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

    /**
     * @param request
     * @return
     * @apiNote 解析请求，获取方法参数
     * @author wjw
     * @date 2022/6/20 11:17
     */
    protected Object[] getMethodArgumentValues(FullHttpRequest request) throws Exception {
        MethodParameter[] parameters = getMethodParameters();
        // 如果没有参数
        if (ObjectUtils.isEmpty(parameters)) {
            return EMPTY_ARGS;
        }

        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            MethodParameter parameter = parameters[i];
            // 名称解析器
            parameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
            // 解析器不支持该参数，则抛出异常
            if (!this.resolvers.supportsParameter(parameter)) {
                throw new IllegalStateException(formatArgumentError(parameter, "No suitable resolver"));
            }
            try {
                args[i] = this.resolvers.resolveArgument(parameter, request);
            } catch (Exception ex) {
                // Leave stack trace for later, exception may actually be resolved and handled...
                if (log.isDebugEnabled()) {
                    String exMsg = ex.getMessage();
                    if (exMsg != null && !exMsg.contains(parameter.getExecutable().toGenericString())) {
                        log.debug(formatArgumentError(parameter, exMsg));
                    }
                }
                throw ex;
            }
        }

        return args;
    }

    protected static String formatArgumentError(MethodParameter param, String message) {
        return "Could not resolve parameter [" + param.getParameterIndex() + "] in " +
                param.getExecutable().toGenericString() + (StringUtils.hasText(message) ? ": " + message : "");
    }

    public void setHandlerMethodReturnValueHandlers(HandlerMethodReturnValueHandlerComposite returnValueHandlers) {
        this.returnValueHandlers = returnValueHandlers;
    }

    public void setHandlerMethodArgumentResolvers(HandlerMethodArgumentResolverComposite argumentResolvers) {
        this.resolvers = argumentResolvers;
    }
}
