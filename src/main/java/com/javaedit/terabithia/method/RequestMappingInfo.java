package com.javaedit.terabithia.method;

import com.javaedit.terabithia.exception.HttpRequestMethodNotSupportedException;
import com.javaedit.terabithia.exception.ServletException;
import com.javaedit.terabithia.method.annotation.RequestMethod;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import lombok.Getter;
import org.springframework.lang.Nullable;

import java.util.HashSet;
import java.util.Objects;

/**
 * @author wjw
 * @description: 存储@RequestMapping相关信息的类，且用于判断请求是否符合条件
 * @title: RequestMappingInfo
 * @date 2022/6/11 14:47
 */
@Getter
public class RequestMappingInfo {

    private String path;

    private RequestMethod[] methods = new RequestMethod[0];

    public RequestMappingInfo(String path, RequestMethod[] methods) {
        this.path = path;
        this.methods = methods;
    }

    public RequestMappingInfo combine(RequestMappingInfo other) {
        StringBuilder path = new StringBuilder();
        if (null != this.getPath()) {
            path.append(this.getPath());
        }
        if (null != other.getPath()) {
            path.append(other.getPath());
        }
        HashSet<RequestMethod> methods = new HashSet<>();
        for (RequestMethod method : this.getMethods()) {
            methods.add(method);
        }
        for (RequestMethod method : other.getMethods()) {
            methods.add(method);
        }
        return new RequestMappingInfo(path.toString().replace("//", "/"), methods.toArray(new RequestMethod[methods.size()]));
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * @param request
     * @return
     * @apiNote 判断请求是否符合条件
     * @description 因为根据url就能找到此RequestMappingInfo，需要判断get、post等，所以需要此方法
     * @author wjw
     * @date 2022/6/11 15:28
     */
    @Nullable
    public RequestMappingInfo getMatchingCondition(FullHttpRequest request) throws ServletException {
        HttpMethod httpMethod = request.method();
        if (methods.length > 0) {
            boolean matchMethod = false;
            for (RequestMethod method : methods) {
                if (method.name().equals(httpMethod.name())) {
                    matchMethod = true;
                    break;
                }
            }
            if (!matchMethod) {
                String[] allowedMethods = new String[methods.length];
                for (int i = 0; i < methods.length; i++) {
                    allowedMethods[i] = methods[i].name();
                }
                throw new HttpRequestMethodNotSupportedException(httpMethod.name(), allowedMethods);
            }
        }
        return this;
    }

    public static class Builder {
        private String path;
        private RequestMethod[] methods = new RequestMethod[0];

        public Builder paths(String path) {
            this.path = path;
            return this;
        }

        public Builder methods(RequestMethod... methods) {
            this.methods = methods;
            return this;
        }

        public RequestMappingInfo build() {
            if (null != path && !path.startsWith("/")) {
                path = "/" + path.replace("//", "/");
            }
            return new RequestMappingInfo(path, methods);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestMappingInfo that = (RequestMappingInfo) o;
        return Objects.equals(getPath(), that.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPath());
    }
}
