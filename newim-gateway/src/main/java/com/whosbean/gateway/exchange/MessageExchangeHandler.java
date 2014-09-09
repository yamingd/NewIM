package com.whosbean.gateway.exchange;

import com.whosbean.gateway.connection.ChannelsHolder;
import com.whosbean.newim.common.MessageUtil;
import com.whosbean.newim.entity.ExchangeMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

/**
 * Created by yaming_deng on 14-9-9.
 */
public class MessageExchangeHandler extends ChannelInboundHandlerAdapter {

    protected static Logger logger = LoggerFactory.getLogger(MessageExchangeHandler.class);

    private ExecutorService executor = null;

    public MessageExchangeHandler(){
        //executor = Executors.newFixedThreadPool(10);
    }

    /**
     * 接收到新的连接
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        logger.info("channelActive: " + ctx.channel().hashCode());
    }

    /**
     * 读取新消息
     */
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        logger.info("channelRead: " + ctx.channel().hashCode());

        /**
         * after receiving
         * 1. find client's channel
         * 2. send message in async way
         * 3. ack back to
         */

        ExchangeMessage message = null;
        try {

            message = MessageUtil.asT(ExchangeMessage.class, (ByteBuf)msg);

        } catch (IOException e) {
            logger.error("ExchangeMessage解析错误", e);
            ack(ctx.channel(), "ERR");
            return;
        }

        for (Integer cid : message.getChannelIds()){
            Channel c = ChannelsHolder.get(cid);
            if (c != null){
                ack(c, message.getMessage());
            }
        }

        ack(ctx.channel(), "OK");

        ctx.fireChannelRead(msg);
    }

    private void ack(final Channel ctx, String msg) {
        ChannelsHolder.ack(logger, ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelReadComplete: " + ctx.channel().hashCode());
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
