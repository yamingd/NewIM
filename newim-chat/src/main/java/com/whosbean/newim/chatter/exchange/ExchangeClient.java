package com.whosbean.newim.chatter.exchange;

import com.google.common.collect.Lists;
import com.whosbean.newim.common.MessageUtil;
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

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yaming_deng on 14-9-9.
 */
public class ExchangeClient {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private volatile boolean enabled;
    private String host;
    private Integer port;

    private List<Channel> channelList = Lists.newArrayList();
    private AtomicInteger seq = new AtomicInteger();
    private final Bootstrap b = new Bootstrap(); // (1)
    private NioEventLoopGroup workerGroup;

    public ExchangeClient(String host, Integer port) {
        this.host = host;
        this.port = port;
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
            System.out.println("channelActive: " + ctx.channel());
            channelList.add(ctx.channel());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            String jsonString = new String((byte[])msg);
            System.out.println("channelRead: " + ctx.channel() + " --> " + jsonString);
            ctx.fireChannelRead(msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            channelList.remove(ctx.channel());
            cause.printStackTrace();
            ctx.close();
            renewChannel();
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelInactive: " + ctx.channel());
            channelList.remove(ctx.channel());
            ctx.fireChannelInactive();
            renewChannel();
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
        int pool = workerGroup.executorCount();
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

            // Start the client.
            for(int i=0; i<pool; i++){
                renewChannel();
            }

        } catch (Exception e){
            e.printStackTrace();
            workerGroup.shutdownGracefully();
        }
    }

    private void renewChannel(){
        new Thread(new Runnable() {
            @Override
            public void run() {

                ChannelFuture f = b.connect(host, port); // (5)

                f.addListener(new GenericFutureListener<Future<? super java.lang.Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {
                        if(future.cause() != null){
                            future.cause().printStackTrace();
                        }
                    }
                });

                try {
                    f.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private Channel get(){
        long id = seq.getAndIncrement();
        id = id % channelList.size();
        Channel c = channelList.get((int)id);
        return c;
    }

    public void send(final ExchangeMessage message) throws Exception {
        if (!this.enabled){
            throw new Exception("Exchange Client has been disabled. host=" + host + ":" + port);
        }

        byte[] bytes = MessageUtil.asBytes(message);
        postSent(message, bytes, 3);

    }

    private void postSent(final ExchangeMessage message, final byte[] bytes, final int trylimit) {
        final Channel c = get();
        final ByteBuf data = c.config().getAllocator().buffer(bytes.length); // (2)
        data.writeBytes(bytes);
        ChannelFuture cf = c.writeAndFlush(data);
        cf.addListener(new GenericFutureListener<Future<Void>>() {
            @Override
            public void operationComplete(Future<Void> future) throws Exception {
                if (future.cause() != null) {
                    logger.error("发送消息错误. host=" + host + ":" + port + ", messageId=" + message.messageId, future.cause());
                    c.close();
                    channelList.remove(c);
                    if (trylimit > 0){
                        postSent(message, bytes, trylimit - 1);
                    }
                }else{
                    logger.info("发送消息成功. messageId=" + message.messageId);
                }
            }
        });
    }

    public void stop(){
        workerGroup.shutdownGracefully();
        logger.info("Stop Exchange Client. host=" + host+":"+port);
    }
}
