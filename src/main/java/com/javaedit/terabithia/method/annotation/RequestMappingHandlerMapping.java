package com.javaedit.terabithia.method.annotation;

import com.javaedit.terabithia.annotation.RequestMapping;
import com.javaedit.terabithia.handler.web.HandlerExecutionChain;
import com.javaedit.terabithia.handler.web.HandlerInterceptor;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
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
 * @description: request????????????????????????
 * @update wjw 2022/6/11 10:28
 * @since 3.1
 */
@Slf4j
@Component
public class RequestMappingHandlerMapping implements ApplicationContextAware, InitializingBean, BeanNameAware {

    private ApplicationContext applicationContext;
    private String beanName;

    private final MappingRegistry mappingRegistry = new MappingRegistry();
    /**
     * ?????????
     */
    private final List<HandlerInterceptor> interceptors = new ArrayList<>();

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
        // ?????????????????????????????????,???????????????????????????????????????
        handlerMethodsInitialized(getHandlerMethods());
    }

    public Map<RequestMappingInfo, HandlerMethod> getHandlerMethods() {
        return Collections.unmodifiableMap(
                this.mappingRegistry.getRegistrations().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().handlerMethod)));
    }

    public void addInterceptor(HandlerInterceptor interceptor) {
        this.interceptors.add(interceptor);
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
        // ???????????????handler?????????
        if (null != beanType && isHandler(beanType)) {
            detectHandlerMethods(beanName);
        }
    }

    protected void detectHandlerMethods(Object handler) {
        // handler??????????????????????????????bean????????????????????????
        Class<?> handlerType = (handler instanceof String ? applicationContext.getType((String) handler) : handler.getClass());
        // ???????????????class?????????handlerType????????????
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
            // ?????????????????????
            Method invocableMethod = AopUtils.selectInvocableMethod(method, userType);
            // ????????????????????????
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
     * @apiNote ???????????????RequestMapping??????
     * @author wjw
     * @date 2022/6/13 14:09
     */
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        // ????????????RequestMapping??????
        RequestMappingInfo info = createRequestMappingInfo(method);
        if (info != null) {
            // ?????????RequestMapping??????
            RequestMappingInfo typeInfo = createRequestMappingInfo(handlerType);
            if (typeInfo != null) {
                // ??????????????????
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

    public HandlerExecutionChain getHandler(FullHttpRequest request) throws Exception {
        // ???????????????url
        String uri = initLookupPath(request);
        RequestMappingInfo mappingInfo = this.mappingRegistry.getMappingsByDirectPath(uri);
        if (null == mappingInfo) {
            return null;
        }
        // ????????????????????????
        mappingInfo = mappingInfo.getMatchingCondition(request);
        if (null == mappingInfo) {
            return null;
        }

        MappingRegistration registration = this.mappingRegistry.getRegistrations().get(mappingInfo);
        if (null == registration) {
            return null;
        }
        HandlerMethod handler = registration.getHandlerMethod();
        // ??????bean?????????????????????????????????????????????
        if (null != handler) {
            handler = handler.createWithResolvedBean();
        }
        return getHandlerExecutionChain(handler, request);
    }

    /**
     * @param request
     * @return
     * @apiNote ???????????????????????????????????????
     * @author wjw
     * @date 2022/6/17 17:11
     */
    protected String initLookupPath(FullHttpRequest request) throws URISyntaxException {
        URI uri = new URI(request.uri());
        return uri.getPath();
    }

    protected HandlerExecutionChain getHandlerExecutionChain(HandlerMethod handler, FullHttpRequest request) {
        HandlerExecutionChain chain = new HandlerExecutionChain(handler);
        // ???????????????
        for (HandlerInterceptor interceptor : this.interceptors) {
            if (interceptor.match(request)) {
                chain.addInterceptor(interceptor);
            }
        }
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
            // ????????????
            validateMethodMapping(handlerMethod, mapping);
            this.pathLookup.put(mapping.getPath(), mapping);
            this.registry.put(mapping, new MappingRegistration(mapping, handlerMethod, mapping.getPath()));
        }

        /**
         * @param handlerMethod
         * @param mapping
         * @return
         * @apiNote ????????????????????????????????????handler??????
         */
        private void validateMethodMapping(HandlerMethod handlerMethod, RequestMappingInfo mapping) {
            MappingRegistration registration = this.registry.get(mapping);
            HandlerMethod existingHandlerMethod = (registration != null ? registration.getHandlerMethod() : null);
            if (existingHandlerMethod != null && !existingHandlerMethod.equals(handlerMethod)) {
                throw new IllegalStateException(
                        "Ambiguous mapping. Cannot map '" + handlerMethod.getBean() + "' method \n" +
                                handlerMethod + "\nto " + mapping + ": There is already '" +
                                existingHandlerMethod.getBean() + "' bean method\n" + existingHandlerMethod + " mapped.");
            }
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
     * @description: ??????????????????????????????????????????
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
