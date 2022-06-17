package com.javaedit.terabithia.method.annotation;

import com.javaedit.terabithia.annotation.RequestMapping;
import com.javaedit.terabithia.handler.web.HandlerExecutionChain;
import com.javaedit.terabithia.method.HandlerMethod;
import com.javaedit.terabithia.method.RequestMappingInfo;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Creates {@link RequestMappingInfo} instances from type and method-level
 * {@link RequestMapping @RequestMapping} annotations in
 * {@link Controller @Controller} classes.
 *
 * <p><strong>Deprecation Note:</strong></p> In 5.2.4,
 * {@link #setUseSuffixPatternMatch(boolean) useSuffixPatternMatch} and
 * {@link #setUseRegisteredSuffixPatternMatch(boolean) useRegisteredSuffixPatternMatch}
 * were deprecated in order to discourage use of path extensions for request
 * mapping and for content negotiation (with similar deprecations in
 * {@link org.springframework.web.accept.ContentNegotiationManagerFactoryBean
 * ContentNegotiationManagerFactoryBean}). For further context, please read issue
 * <a href="https://github.com/spring-projects/spring-framework/issues/24179">#24719</a>.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Sam Brannen
 * @description: request请求处理器映射器
 * @update wjw 2022/6/11 10:28
 * @since 3.1
 */
@Slf4j
@Component
public class RequestMappingHandlerMapping implements ApplicationContextAware, InitializingBean, BeanNameAware {

    private ApplicationContext applicationContext;
    private String beanName;

    private final MappingRegistry mappingRegistry = new MappingRegistry();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (String beanName : getCandidateBeanNames()) {
            processCandidateBean(beanName);
        }
        // 初始化完成后执行的方法,由子类实现，父类只提供调试
        handlerMethodsInitialized(getHandlerMethods());
    }

    public Map<RequestMappingInfo, HandlerMethod> getHandlerMethods() {
        return Collections.unmodifiableMap(
                this.mappingRegistry.getRegistrations().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().handlerMethod)));
    }

    protected void handlerMethodsInitialized(Map<RequestMappingInfo, HandlerMethod> handlerMethods) {
        // Total includes detected mappings + explicit registrations via registerMapping
        int total = handlerMethods.size();
        if ((log.isTraceEnabled() && total == 0) || (log.isDebugEnabled() && total > 0)) {
            log.debug(total + " mappings in " + this.beanName);
        }
    }

    protected String[] getCandidateBeanNames() {
        return BeanFactoryUtils.beanNamesForTypeIncludingAncestors(applicationContext, Object.class);
    }

    protected void processCandidateBean(String beanName) {
        Class<?> beanType = applicationContext.getType(beanName);
        // 判断只有是handler才继续
        if (null != beanType && isHandler(beanType)) {
            detectHandlerMethods(beanName);
        }
    }

    protected void detectHandlerMethods(Object handler) {
        // handler使用字符串是防止有些bean还未初始化完成？
        Class<?> handlerType = (handler instanceof String ? applicationContext.getType((String) handler) : handler.getClass());
        // 获取原始的class，防止handlerType是代理类
        Class<?> userType = ClassUtils.getUserClass(handlerType);
        Map<Method, RequestMappingInfo> methods = MethodIntrospector.selectMethods(userType,
                (MethodIntrospector.MetadataLookup<RequestMappingInfo>) method -> {
                    try {
                        return getMappingForMethod(method, userType);
                    } catch (Throwable ex) {
                        throw new IllegalStateException("Invalid mapping on handler class [" +
                                userType.getName() + "]: " + method, ex);
                    }
                });
        methods.forEach((method, mapping) -> {
            // 查找可调用方法
            Method invocableMethod = AopUtils.selectInvocableMethod(method, userType);
            // 注册请求处理方法
            registerHandlerMethod(handler, invocableMethod, mapping);
        });
    }

    protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
        this.mappingRegistry.register(mapping, handler, method);
    }

    /**
     * @param method
     * @param handlerType
     * @return
     * @apiNote 生成方法的RequestMapping信息
     * @author wjw
     * @date 2022/6/13 14:09
     */
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        // 方法上的RequestMapping注解
        RequestMappingInfo info = createRequestMappingInfo(method);
        if (info != null) {
            // 类上的RequestMapping注解
            RequestMappingInfo typeInfo = createRequestMappingInfo(handlerType);
            if (typeInfo != null) {
                // 合并两个注解
                info = typeInfo.combine(info);
            }
        }
        return info;

    }

    private RequestMappingInfo createRequestMappingInfo(AnnotatedElement element) {
        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class);
        if (null == requestMapping) {
            return null;
        }
        return RequestMappingInfo.builder()
                .paths(requestMapping.path())
                .methods(requestMapping.method())
                .build();
    }

    protected boolean isHandler(Class<?> beanType) {
        return (AnnotatedElementUtils.hasAnnotation(beanType, Controller.class) ||
                AnnotatedElementUtils.hasAnnotation(beanType, RequestMapping.class));
    }

    public HandlerExecutionChain getHandler(FullHttpRequest request) {
        // 获取请求的url
        String uri = request.uri();
        RequestMappingInfo mappingInfo = this.mappingRegistry.getMappingsByDirectPath(uri);
        if (null == mappingInfo) {
            return null;
        }
        MappingRegistration registration = this.mappingRegistry.getRegistrations().get(mappingInfo);
        if (null == registration) {
            return null;
        }
        HandlerMethod handler = registration.getHandlerMethod();
        // 因为bean有可能是字符串，所以需要转换下
        if (null != handler) {
            handler = handler.createWithResolvedBean();
        }
        return getHandlerExecutionChain(handler, request);
    }

    protected HandlerExecutionChain getHandlerExecutionChain(HandlerMethod handler, FullHttpRequest request) {
        HandlerExecutionChain chain = new HandlerExecutionChain(handler);
        return chain;
    }

    class MappingRegistry {

        private final Map<String, RequestMappingInfo> pathLookup = new HashMap<>();
        private final Map<RequestMappingInfo, MappingRegistration> registry = new HashMap<>();

        public Map<RequestMappingInfo, MappingRegistration> getRegistrations() {
            return this.registry;
        }

        public RequestMappingInfo getMappingsByDirectPath(String urlPath) {
            return this.pathLookup.get(urlPath);
        }

        public void register(RequestMappingInfo mapping, Object handler, Method method) {
            HandlerMethod handlerMethod = createHandlerMethod(handler, method);
            this.pathLookup.put(mapping.getPath(), mapping);
            this.registry.put(mapping, new MappingRegistration(mapping, handlerMethod, mapping.getPath()));
        }
    }

    protected HandlerMethod createHandlerMethod(Object handler, Method method) {
        if (handler instanceof String) {
            return new HandlerMethod((String) handler, applicationContext.getAutowireCapableBeanFactory(), method);
        }
        return new HandlerMethod(handler, method);
    }

    /**
     * @author wjw
     * @description: 存储一个请求处理器的相关信息
     * @title: MappingRegistration
     * @date 2022/6/14 10:10
     */
    static class MappingRegistration {
        private final RequestMappingInfo mapping;

        private final HandlerMethod handlerMethod;

        private final String directPath;

        public MappingRegistration(RequestMappingInfo mapping, HandlerMethod handlerMethod, String directPath) {
            this.mapping = mapping;
            this.handlerMethod = handlerMethod;
            this.directPath = directPath;
        }

        public RequestMappingInfo getMapping() {
            return mapping;
        }

        public HandlerMethod getHandlerMethod() {
            return handlerMethod;
        }

        public String getDirectPath() {
            return directPath;
        }
    }
}
