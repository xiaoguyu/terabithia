package com.javaedit.terabithia.method.annotation;

import com.javaedit.terabithia.method.HandlerMethod;
import com.javaedit.terabithia.method.support.HandlerMethodReturnValueHandler;
import com.javaedit.terabithia.method.support.handler.HandlerMethodReturnValueHandlerComposite;
import com.javaedit.terabithia.method.support.handler.RequestResponseBodyMethodProcessor;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wjw
 * @description: 请求映射处理器适配器
 * @title: RequestMappingHandlerAdapter
 * @date 2022/6/14 11:21
 */
@Component
public class RequestMappingHandlerAdapter implements InitializingBean {

    @Nullable
    private HandlerMethodReturnValueHandlerComposite returnValueHandlers;

    public FullHttpResponse handle(FullHttpRequest request, HandlerMethod handlerMethod) throws Exception {
        InvocableHandlerMethod invocableMethod = new InvocableHandlerMethod(handlerMethod);
        if (this.returnValueHandlers != null) {
            invocableMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);
        }
        return invocableMethod.invokeAndHandle(request);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.returnValueHandlers == null) {
            List<HandlerMethodReturnValueHandler> handlers = getDefaultReturnValueHandlers();
            this.returnValueHandlers = new HandlerMethodReturnValueHandlerComposite().addHandlers(handlers);
        }
    }

    /**
     * @return
     * @apiNote 获取内置的返回值处理器
     * @author wjw
     * @date 2022/6/15 11:48
     */
    protected List<HandlerMethodReturnValueHandler> getDefaultReturnValueHandlers() {
        List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>();

        handlers.add(new RequestResponseBodyMethodProcessor());
        return handlers;
    }
}
