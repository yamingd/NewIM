package com.whosbean.newim.chatter.exchange;

import com.whosbean.newim.entity.ExchangeMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yaming_deng on 14-9-9.
 */
public class ExchangeClient {

    protected Logger logger = null;

    private volatile boolean enabled;
    private String host;
    private Integer port;

    private final Bootstrap b = new Bootstrap(); // (1)
    private NioEventLoopGroup workerGroup;

    public ExchangeClient(String host, Integer port) {
        this.host = host;
        this.port = port;
        this.logger = LoggerFactory.getLogger(this.getClass().getName()+"."+host+"."+port);
        this.enabled = true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public class ClientConnectHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            logger.info("channelActive: " + ctx.channel());

        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            String jsonString = new String((byte[])msg);
            logger.info("channelRead: " + ctx.channel() + " --> " + jsonString);
            ctx.fireChannelRead(msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
             cause.printStackTrace();
            ctx.close();

        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            logger.info("channelInactive: " + ctx.channel() + ", ");
            //channelList.remove(ctx.channel());
            ctx.fireChannelInactive();

        }

    }

    public void start(){

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("Start Exchange Client. host=" + host+":"+port);
                startConnect();
                logger.info("DONE Start Exchange Client. host=" + host+":"+port);
            }
        });

        thread.start();
    }

    private void startConnect(){
        workerGroup = new NioEventLoopGroup();
        try {
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.option(ChannelOption.TCP_NODELAY, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                    pipeline.addLast("bytesDecoder",new ByteArrayDecoder());

                    pipeline.addLast("frameEncoder", new LengthFieldPrepender(4, false));
                    pipeline.addLast("bytesEncoder", new ByteArrayEncoder());

                    pipeline.addLast("handler", new ClientConnectHandler());
                }
            });

        } catch (Exception e){
            e.printStackTrace();
            workerGroup.shutdownGracefully();
        }
    }

    public void send(final ExchangeMessage message) throws Exception {
        if (!this.enabled){
            throw new Exception("Exchange Client has been disabled. host=" + host + ":" + port);
        }

        postSent(message, message.toByteArray(), 3);

    }

    private void postSent(final ExchangeMessage message, final byte[] bytes, final int trylimit) {
        final ChannelFuture f = b.connect(host, port); // (5)
        f.addListener(new GenericFutureListener<Future<? super java.lang.Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if(future.cause() != null){
                    future.cause().printStackTrace();
                }else {
                    final Channel c = f.channel();
                    final ByteBuf data = c.config().getAllocator().buffer(bytes.length); // (2)
                    data.writeBytes(bytes);
                    ChannelFuture cf = c.writeAndFlush(data);
                    cf.addListener(new GenericFutureListener<Future<Void>>() {
                        @Override
                        public void operationComplete(Future<Void> future) throws Exception {
                            if (future.cause() != null) {
                                logger.error("发送消息错误. host=" + host + ":" + port + ", messageId=" + message.getMessageId(), future.cause());
                                c.close();
                                if (trylimit > 0){
                                    postSent(message, bytes, trylimit - 1);
                                }
                            }else{
                                logger.info("发送消息成功. messageId=" + message.getMessageId());
                            }
                        }
                    });
                }
            }
        });

    }

    public void stop(){
        workerGroup.shutdownGracefully();
        logger.info("Stop Exchange Client. host=" + host+":"+port);
    }
}
