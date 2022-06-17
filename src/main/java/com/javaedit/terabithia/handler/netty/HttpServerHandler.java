package com.javaedit.terabithia.handler.netty;

import com.javaedit.terabithia.handler.web.HandlerExecutionChain;
import com.javaedit.terabithia.method.annotation.RequestMappingHandlerAdapter;
import com.javaedit.terabithia.method.annotation.RequestMappingHandlerMapping;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author wjw
 * @description: netty的http请求处理器
 * @title: HttpServerHandler
 * @date 2022/6/14 10:34
 */
@Slf4j
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private ApplicationContext context;
    private RequestMappingHandlerMapping handlerMapping;
    private RequestMappingHandlerAdapter handlerAdapter;

    public HttpServerHandler(ApplicationContext context) {
        this.context = context;
        this.handlerMapping = context.getBean(RequestMappingHandlerMapping.class);
        this.handlerAdapter = context.getBean(RequestMappingHandlerAdapter.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        this.doDispatch(ctx, request);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private void doDispatch(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        HandlerExecutionChain mappedHandler = null;
        try {
            mappedHandler = this.handlerMapping.getHandler(request);

            if (mappedHandler == null) {
                noHandlerFound(ctx);
                return;
            }
            // 执行拦截器-前置方法
            if (!mappedHandler.applyPreHandle(ctx, request)) {
                return;
            }
            // 执行handler
            FullHttpResponse response = handlerAdapter.handle(request, mappedHandler.getHandler());

            // 执行拦截器-后置方法
            mappedHandler.applyPostHandle(ctx, request, response);

            processDispatchResult(ctx, request, response, mappedHandler);
        } catch (Exception ex) {
            triggerAfterCompletion(ctx, request, mappedHandler, ex);
        }
    }

    /**
     * @param ctx
     * @param request
     * @param response
     * @param mappedHandler
     * @return
     * @apiNote 处理请求结果
     * @author wjw
     * @date 2022/6/17 11:21
     */
    private void processDispatchResult(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response, HandlerExecutionChain mappedHandler) {
        // 执行拦截器-完成方法
        if (mappedHandler != null) {
            mappedHandler.triggerAfterCompletion(ctx, request, null);
        }

        boolean keepAlive = HttpUtil.isKeepAlive(request);
        if (!keepAlive) {
            response.headers().set(CONNECTION, CLOSE);
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.write(response);
        }
    }

    /**
     * @param ctx
     * @param request
     * @param mappedHandler
     * @param ex
     * @return
     * @apiNote 异常也会触发拦截器的完成方法
     * @author wjw
     * @date 2022/6/17 11:13
     */
    private void triggerAfterCompletion(ChannelHandlerContext ctx, FullHttpRequest request, HandlerExecutionChain mappedHandler, Exception ex) throws Exception {
        if (mappedHandler != null) {
            mappedHandler.triggerAfterCompletion(ctx, request, ex);
        }
        throw ex;
    }

    /**
     * @param ctx
     * @return
     * @apiNote 找不到处理器，则返回404
     * @author wjw
     * @date 2022/6/17 11:13
     */
    private void noHandlerFound(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        String result = "http state code:" + response.status().code() + "\nNot Found";
        response.content().writeBytes(result.getBytes(StandardCharsets.UTF_8));
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

        response.headers().set(CONNECTION, CLOSE);
        ctx.write(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * @param ctx
     * @param cause
     * @return
     * @apiNote 异常关闭连接，返回错误信息
     * @author wjw
     * @date 2022/6/14 10:47
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause = cause.getCause() == null ? cause : cause.getCause();
        log.error("request error ", cause);

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        String result = "http state code:" + response.status().code() + "\n" + cause.getMessage();
        response.content().writeBytes(result.getBytes(StandardCharsets.UTF_8));
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

        response.headers().set(CONNECTION, CLOSE);
        ctx.write(response).addListener(ChannelFutureListener.CLOSE);
    }
}
