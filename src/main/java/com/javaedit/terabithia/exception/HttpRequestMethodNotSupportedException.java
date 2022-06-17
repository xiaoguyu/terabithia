package com.javaedit.terabithia.exception;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.Collection;

/**
 * Exception thrown when a request handler does not support a
 * specific request method.
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
@SuppressWarnings("serial")
public class HttpRequestMethodNotSupportedException extends ServletException {

    private final String method;

    @Nullable
    private final String[] supportedMethods;


    /**
     * Create a new HttpRequestMethodNotSupportedException.
     *
     * @param method           the unsupported HTTP request method
     * @param supportedMethods the actually supported HTTP methods
     * @param msg              the detail message
     */
    public HttpRequestMethodNotSupportedException(String method, @Nullable String[] supportedMethods, String msg) {
        super(msg);
        this.method = method;
        this.supportedMethods = supportedMethods;
    }

    /**
     * Create a new HttpRequestMethodNotSupportedException.
     *
     * @param method           the unsupported HTTP request method
     * @param supportedMethods the actually supported HTTP methods (may be {@code null})
     */
    public HttpRequestMethodNotSupportedException(String method, @Nullable String[] supportedMethods) {
        this(method, supportedMethods, "Request method '" + method + "' not supported");
    }

    /**
     * Create a new HttpRequestMethodNotSupportedException.
     *
     * @param method           the unsupported HTTP request method
     * @param supportedMethods the actually supported HTTP methods (may be {@code null})
     */
    public HttpRequestMethodNotSupportedException(String method, @Nullable Collection<String> supportedMethods) {
        this(method, (supportedMethods != null ? StringUtils.toStringArray(supportedMethods) : null));
    }
}
