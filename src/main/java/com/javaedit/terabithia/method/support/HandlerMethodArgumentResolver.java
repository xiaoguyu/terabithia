package com.javaedit.terabithia.method.support;

import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;

/**
 * Strategy interface for resolving method parameters into argument values in
 * the context of a given request.
 *
 * @author Arjen Poutsma
 * @update wjw 2022/6/20 10:50
 * @see HandlerMethodReturnValueHandler
 * @since 3.1
 */
public interface HandlerMethodArgumentResolver {

    /**
     * Whether the given {@linkplain MethodParameter method parameter} is
     * supported by this resolver.
     *
     * @param parameter the method parameter to check
     * @return {@code true} if this resolver supports the supplied parameter;
     * {@code false} otherwise
     */
    boolean supportsParameter(MethodParameter parameter);

    /**
     * Resolves a method parameter into an argument value from a given request.
     * A {@link ModelAndViewContainer} provides access to the model for the
     * request. A {@link WebDataBinderFactory} provides a way to create
     * a {@link WebDataBinder} instance when needed for data binding and
     * type conversion purposes.
     *
     * @param parameter  the method parameter to resolve. This parameter must
     *                   have previously been passed to {@link #supportsParameter} which must
     *                   have returned {@code true}.
     * @param webRequest the current request
     * @return the resolved argument value, or {@code null} if not resolvable
     * @throws Exception in case of errors with the preparation of argument values
     */
    @Nullable
    Object resolveArgument(MethodParameter parameter, FullHttpRequest webRequest) throws Exception;

}
