package com.whosbean.newim;

import com.whosbean.newim.gateway.GatewayConfig;
import com.whosbean.newim.gateway.exchange.MessageExchangeHandler;
import com.whosbean.newim.gateway.handler.HttpSessionHandler;
import com.whosbean.newim.gateway.handler.WsConnectedHandler;
import com.whosbean.newim.server.ServerStarter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.websocketx.WebSocket13FrameEncoder;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Created by yaming_deng on 14-9-9.
 */
public class GatewayMain implements ServerStarter {

    public class WebsocketServerThread extends Thread{

        @Override
        public void run() {
            startWebsocketServer(GatewayConfig.current);
        }
    }

    public class MessageSenderServerThread extends Thread{

        @Override
        public void run() {
            startSenderServer(GatewayConfig.current);
        }
    }

    /**
     * 启动推送服务 8080端口
     */
    public void start() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-gateway.xml");
        GatewayConfig prop = context.getBean("gatewayConfig", GatewayConfig.class);
        Assert.notNull(prop, "gatewayConfig bean is NULL.");
        new WebsocketServerThread().start();
        new MessageSenderServerThread().start();
    }

    private void startWebsocketServer(GatewayConfig prop) {
        Map server = prop.get(Map.class, "websocket");
        Integer actSize = (Integer)server.get("actors");
        Integer workerSize = (Integer)server.get("workers");
        String host = (String)server.get("ip");
        int port = (Integer)server.get("port");

        EventLoopGroup parentGroup = new NioEventLoopGroup(actSize); // 用于接收发来的连接请求
        EventLoopGroup childGroup = new NioEventLoopGroup(workerSize); // 用于处理parentGroup接收并注册给child的连接中的信息
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap(); // 服务器助手类
            // 简历新的accept连接，用于构建serverSocketChannel的工厂类
            serverBootstrap.group(parentGroup, childGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(
                                    new HttpRequestDecoder(),
                                    new HttpObjectAggregator(65536),
                                    new HttpSessionHandler(),
                                    new WsConnectedHandler(),
                                    new HttpResponseEncoder(),
                                    new WebSocket13FrameEncoder(false)
                            );
                        }
                    });

            serverBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            serverBootstrap.option(ChannelOption.TCP_NODELAY, true);
            serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);

            System.out.println("start Websocket server " + host + ":" + port + " ... ");
            ChannelFuture f = serverBootstrap.bind(host, port).sync();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            childGroup.shutdownGracefully();
            parentGroup.shutdownGracefully();
        }
    }

    private void startSenderServer(GatewayConfig prop) {
        Map server = prop.get(Map.class, "exchange");
        Integer actSize = (Integer)server.get("actors");
        Integer workerSize = (Integer)server.get("workers");
        String host = (String)server.get("ip");
        int port = (Integer)server.get("port");

        EventLoopGroup parentGroup = new NioEventLoopGroup(actSize); // 用于接收发来的连接请求
        EventLoopGroup childGroup = new NioEventLoopGroup(workerSize); // 用于处理parentGroup接收并注册给child的连接中的信息
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap(); // 服务器助手类
            // 简历新的accept连接，用于构建serverSocketChannel的工厂类
            serverBootstrap.group(parentGroup, childGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                            pipeline.addLast("bytesDecoder",new ByteArrayDecoder());

                            pipeline.addLast("frameEncoder", new LengthFieldPrepender(4, false));
                            pipeline.addLast("bytesEncoder", new ByteArrayEncoder());

                            pipeline.addLast("handler", new MessageExchangeHandler());
                        }
                    });

            serverBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            serverBootstrap.option(ChannelOption.TCP_NODELAY, true);
            serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);

            System.out.println("start Message Sender server " + host+":"+port + " ... ");
            ChannelFuture f = serverBootstrap.bind(host, port).sync();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            childGroup.shutdownGracefully();
            parentGroup.shutdownGracefully();
        }
    }

    /**
     * 入口
     * @param args
     */
    public static void main(String[] args) {
        new GatewayMain().start();
    }

}
