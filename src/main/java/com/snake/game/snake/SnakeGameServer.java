package com.snake.game.snake;

import com.alibaba.fastjson.JSON;
import com.snake.game.snake.gameengine.NormalGameEngine;
import com.snake.game.snake.handler.HttpRequestHandler;
import com.snake.game.snake.handler.SnakeGameHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;

/**
 * websocket 聊天室服务器
 */
@Service
public class SnakeGameServer implements InitializingBean {

    private int port;
    private ChannelGroup channels;
    public SnakeGameServer() {
        channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    public void run(){
        //启动游戏
        Executors.newFixedThreadPool(1).submit(() -> {
            EventLoopGroup bossGroup = new NioEventLoopGroup(2);
            EventLoopGroup workerGroup = new NioEventLoopGroup(3);
            try {
                ServerBootstrap serverBootstrap = new ServerBootstrap();
                serverBootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer() {
                            @Override
                            protected void initChannel(Channel ch) throws Exception {
                                ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast("http-decodec", new HttpRequestDecoder());
                                pipeline.addLast("http-aggregator", new HttpObjectAggregator(65536));
                                pipeline.addLast("http-encodec", new HttpResponseEncoder());
                                pipeline.addLast("http-chunked", new ChunkedWriteHandler());
                                pipeline.addLast("http-request", new HttpRequestHandler("/ws"));
                                pipeline.addLast("webSocket-protocol",new WebSocketServerProtocolHandler("/ws"));
                                pipeline.addLast("webSocket-request", new SnakeGameHandler(channels));
                            }
                        }).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);
                System.out.println("SnakeGameServer 启动了" + port);
                // 绑定端口，开始接收进来的连接
                ChannelFuture f = serverBootstrap.bind(port).sync();
                // 等待服务器  socket 关闭 。
                f.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                System.out.println("SnakeGameServer InterruptedException " + e.getMessage());
            } finally {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
                System.out.println("SnakeGameServer 关闭了");
            }
        });
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.port = 8888;
        run();
    }
}
