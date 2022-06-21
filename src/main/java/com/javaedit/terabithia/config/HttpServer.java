package com.javaedit.terabithia.config;

import com.javaedit.terabithia.handler.netty.HttpServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

/**
 * @author wjw
 * @description: 提供http服务的类，用于配置与启动netty
 * @title: HttpServer
 * @date 2022/6/10 18:02
 */
@Slf4j
public class HttpServer {

    private ApplicationContext context;

    public HttpServer(ApplicationContext context) {
        this.context = context;
    }

    public void start() {
        // 初始化netty相关
        TerabithiaProperties properties = context.getBean(TerabithiaProperties.class);

        final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            final ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    // 临时存放已完成三次握手的请求的队列
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .channel(NioServerSocketChannel.class)
                    // 日志
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new HttpServerCodec())
                                    //把多个消息转换为一个单一的FullHttpRequest或是FullHttpResponse
                                    .addLast(new HttpObjectAggregator(65536))
                                    //压缩Http消息
//						.addLast(new HttpChunkContentCompressor())
                                    //大文件支持
                                    .addLast(new ChunkedWriteHandler())
                                    .addLast(new HttpServerHandler(context));
                        }
                    });

            Integer port = properties.getPort();
            final Channel ch = b.bind(port).sync().channel();
            log.info("***** HttpServer started, port:{} *****", port);
            ch.closeFuture().sync();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
