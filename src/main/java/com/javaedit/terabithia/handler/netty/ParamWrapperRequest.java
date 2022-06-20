package com.javaedit.terabithia.handler.netty;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;

/**
 * @author wjw
 * @description: 将netty的请求再封装一次
 * @title: NettyFullRequest
 * @date 2022/6/20 15:48
 */
@Slf4j
public class ParamWrapperRequest implements FullHttpRequest {

    private final FullHttpRequest request;
    /*
     * 请求参数map
     * 注意：value为不可变数组
     */
    private volatile Map<String, List<String>> parameterMap;

    public ParamWrapperRequest(FullHttpRequest request) {
        this.request = request;
    }

    private synchronized void initParameterMap() {
        if (this.parameterMap != null) {
            return;
        }
        // 获取GET类型参数
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> params = queryStringDecoder.parameters();

        Map<String, List<String>> initMap = new HashMap<>(params);
        // 获取POST类型参数
        if (HttpMethod.POST == request.method()) {
            HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(factory, request);
            List<InterfaceHttpData> datas = decoder.getBodyHttpDatas();
            Attribute attribute;
            String attrName = null;
            String attrValue;
            List<String> mapVal;
            for (InterfaceHttpData data : datas) {
                try {
                    attribute = (Attribute) data;
                    attrName = attribute.getName();
                    attrValue = attribute.getValue();
                    mapVal = initMap.computeIfAbsent(attrName, k -> new ArrayList<>(1));
                    mapVal.add(attrValue);
                } catch (IOException e) {
                    log.error("Request Parameter Get Fail, Name:{}", attrName, e);
                }
            }
        }

        parameterMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : initMap.entrySet()) {
            parameterMap.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
    }

    /**
     * @param paramName
     * @return
     * @apiNote 根据名字获取请求参数
     * @author wjw
     * @date 2022/6/20 15:51
     */
    public List<String> getParameterValues(String paramName) {
        if (null == this.parameterMap) {
            initParameterMap();
        }
        return this.parameterMap.get(paramName);
    }

    /**
     * @return
     * @apiNote 获取所有的请求key
     * @author wjw
     * @date 2022/6/20 15:51
     */
    public Set<String> getParameterNames() {
        if (null == this.parameterMap) {
            initParameterMap();
        }
        return this.parameterMap.keySet();
    }

    /**
     * @return
     * @apiNote 获取请求参数map
     * @author wjw
     * @date 2022/6/20 15:52
     */
    public Map<String, List<String>> getParameterMap() {
        if (null == this.parameterMap) {
            initParameterMap();
        }
        return Collections.unmodifiableMap(this.parameterMap);
    }

    public FullHttpRequest getRequest() {
        return request;
    }

    @Override
    public FullHttpRequest copy() {
        return request.copy();
    }

    @Override
    public FullHttpRequest duplicate() {
        return request.duplicate();
    }

    @Override
    public FullHttpRequest retainedDuplicate() {
        return request.retainedDuplicate();
    }

    @Override
    public FullHttpRequest replace(ByteBuf content) {
        return request.replace(content);
    }

    @Override
    public FullHttpRequest retain(int increment) {
        return request.retain(increment);
    }

    @Override
    public FullHttpRequest retain() {
        return request.retain();
    }

    @Override
    public FullHttpRequest touch() {
        return request.touch();
    }

    @Override
    public FullHttpRequest touch(Object hint) {
        return request.touch(hint);
    }

    @Override
    public FullHttpRequest setProtocolVersion(HttpVersion version) {
        return request.setProtocolVersion(version);
    }

    @Override
    public FullHttpRequest setMethod(HttpMethod method) {
        return request.setMethod(method);
    }

    @Override
    public FullHttpRequest setUri(String uri) {
        return request.setUri(uri);
    }

    @Override
    public ByteBuf content() {
        return request.content();
    }

    @Override
    public HttpMethod getMethod() {
        return request.getMethod();
    }

    @Override
    public HttpMethod method() {
        return request.method();
    }

    @Override
    public String getUri() {
        return request.getUri();
    }

    @Override
    public String uri() {
        return request.uri();
    }

    @Override
    public HttpVersion getProtocolVersion() {
        return request.getProtocolVersion();
    }

    @Override
    public HttpVersion protocolVersion() {
        return request.protocolVersion();
    }

    @Override
    public HttpHeaders headers() {
        return request.headers();
    }

    @Override
    public HttpHeaders trailingHeaders() {
        return request.trailingHeaders();
    }

    @Override
    public DecoderResult getDecoderResult() {
        return request.getDecoderResult();
    }

    @Override
    public DecoderResult decoderResult() {
        return request.decoderResult();
    }

    @Override
    public void setDecoderResult(DecoderResult decoderResult) {
        request.setDecoderResult(decoderResult);
    }

    @Override
    public int refCnt() {
        return request.refCnt();
    }

    @Override
    public boolean release() {
        return request.release();
    }

    @Override
    public boolean release(int i) {
        return request.release(i);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParamWrapperRequest that = (ParamWrapperRequest) o;
        return Objects.equals(getRequest(), that.getRequest());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRequest());
    }
}
