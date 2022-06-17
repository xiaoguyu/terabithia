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
        HandlerExecutionChain handler = this.handlerMapping.getHandler(request);

        if (handler == null) {
            noHandlerFound(ctx);
            return;
        }
        // 执行handler
        FullHttpResponse response = handlerAdapter.handle(request, handler.getHandler());

        boolean keepAlive = HttpUtil.isKeepAlive(request);
        if (!keepAlive) {
            response.headers().set(CONNECTION, CLOSE);
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.write(response);
        }
    }

    private void noHandlerFound(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.content().writeBytes("Not Found".getBytes(StandardCharsets.UTF_8));
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
        log.error("request error " + cause);

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.content().writeBytes(cause.getMessage().getBytes(StandardCharsets.UTF_8));
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

        response.headers().set(CONNECTION, CLOSE);
        ctx.write(response).addListener(ChannelFutureListener.CLOSE);
    }
}
