package com.whosbean.newim.gateway.exchange;

import com.google.common.base.Charsets;
import com.whosbean.newim.entity.ExchangeMessage;
import com.whosbean.newim.gateway.GatewayConfig;
import com.whosbean.newim.gateway.GatewayServerNode;
import com.whosbean.newim.gateway.connection.ChannelsHolder;
import com.whosbean.newim.service.ChatMessageService;
import com.whosbean.newim.service.ChatMessageServiceFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yaming_deng on 14-9-9.
 */
public class MessageExchangeHandler extends SimpleChannelInboundHandler<byte[]> {

    protected static Logger logger = LoggerFactory.getLogger(MessageExchangeHandler.class);

    private ChatMessageService chatMessageService;

    public MessageExchangeHandler(){
        chatMessageService = ChatMessageServiceFactory.get(GatewayConfig.current);
    }

    /**
     * 接收到新的连接
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        logger.info("channelActive: " + ctx.channel().hashCode());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
        logger.info("channelRead: " + ctx.channel().hashCode());
        /**
         * after receiving
         * 1. find client's channel
         * 2. send message in async way
         * 3. ack back to
         */
        ExchangeMessage message = ExchangeMessage.newBuilder().mergeFrom(msg).build();
        int total = 0;
        byte[] bytes = chatMessageService.getBytes(message.getMessageId());
        if (bytes == null){
            //JOIN, QUIT
        }else {
            for (Integer cid : message.getChannelIdList()) {
                Channel c = ChannelsHolder.get(cid);
                if (c != null) {
                    ChannelsHolder.ack(logger, c, bytes, message.getChatPath());
                    total++;
                } else {
                    GatewayServerNode.current.remConnection(message.getChatPath(), cid);
                }
            }
        }

        //TODO:分布式协同是否发完
        //GatewayServerNode.current.outMessage(message.msgPath);

        ack(ctx.channel(), total + "");
    }

    private void ack(final Channel ctx, String msg) {
        final ByteBuf data = Unpooled.copiedBuffer(msg.getBytes(Charsets.UTF_8));
        final ChannelFuture cf = ctx.writeAndFlush(data);
        cf.addListener(new GenericFutureListener<Future<Void>>() {
            @Override
            public void operationComplete(Future<Void> future) throws Exception {
                if (future.cause() != null) {
                    logger.error("发送消息错误. ctx=" + ctx, future.cause());
                    ctx.close();
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("发送消息成功. ctx={}", ctx);
                    }
                }
            }
        });
    }

    /**
     * 连接异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 连接断开，移除连接影射，客户端发起重连
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx)
            throws Exception {
        logger.info("channelInactive: " + ctx.channel().hashCode());
    }

}
