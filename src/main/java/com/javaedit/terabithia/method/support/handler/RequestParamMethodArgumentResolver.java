package com.javaedit.terabithia.method.support.handler;

import com.javaedit.terabithia.handler.netty.ParamWrapperRequest;
import com.javaedit.terabithia.method.support.HandlerMethodArgumentResolver;
import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.validation.DataBinder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wjw
 * @description: 请求参数处理器器，支持基本数据类型或者基本数据类型的数组
 * @title: RequestParamMethodArgumentResolver
 * @date 2022/6/20 11:32
 */
public class RequestParamMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private final Map<MethodParameter, NamedValueInfo> namedValueInfoCache = new ConcurrentHashMap<>(256);

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 暂时只支持基本数据类型或者基本数据类型的数组
        return BeanUtils.isSimpleProperty(parameter.getNestedParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, FullHttpRequest webRequest) throws Exception {
        NamedValueInfo namedValueInfo = getNamedValueInfo(parameter);

        // 获取请求参数值
        Object arg = resolveName(namedValueInfo.name, parameter.nestedIfOptional(), webRequest);

        // 数据类型转换
        DataBinder binder = new DataBinder(null, namedValueInfo.name);
        arg = binder.convertIfNecessary(arg, parameter.getParameterType(), parameter);

        return arg;
    }

    /**
     * @param name
     * @param parameter
     * @param request
     * @return
     * @apiNote 将给定的名和类型解析成指定的参数值
     * @author wjw
     * @date 2022/6/20 16:08
     */
    protected Object resolveName(String name, MethodParameter parameter, FullHttpRequest request) throws Exception {
        ParamWrapperRequest paramWrapperRequest = request instanceof ParamWrapperRequest ? (ParamWrapperRequest) request : new ParamWrapperRequest(request);
        List<String> paramValues = paramWrapperRequest.getParameterValues(name);

        Object arg = null;
        if (paramValues != null) {
            arg = paramValues.size() == 1 ? paramValues.get(0) : paramValues.toArray(new String[paramValues.size()]);
        }
        return arg;
    }

    /**
     * @param parameter
     * @return
     * @apiNote 解析@RequestParam注解
     * @author wjw
     * @date 2022/6/20 14:57
     */
    private NamedValueInfo getNamedValueInfo(MethodParameter parameter) {
        NamedValueInfo namedValueInfo = this.namedValueInfoCache.get(parameter);
        if (namedValueInfo == null) {
            // TODO
            // 暂时还不处理@RequestParam注解
            namedValueInfo = new NamedValueInfo(parameter.getParameterName(), false, null);
            if (namedValueInfo.name.isEmpty()) {
                String name = parameter.getParameterName();
                if (name == null) {
                    throw new IllegalArgumentException(
                            "Name for argument of type [" + parameter.getNestedParameterType().getName() +
                                    "] not specified, and parameter name information not found in class file either.");
                }
            }
            this.namedValueInfoCache.put(parameter, namedValueInfo);
        }
        return namedValueInfo;
    }


    /**
     * Represents the information about a named value, including name, whether it's required and a default value.
     */
    protected static class NamedValueInfo {

        private final String name;

        private final boolean required;

        @Nullable
        private final String defaultValue;

        public NamedValueInfo(String name, boolean required, @Nullable String defaultValue) {
            this.name = name;
            this.required = required;
            this.defaultValue = defaultValue;
        }
    }
}
