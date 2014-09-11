package com.whosbean.newim.gateway.exchange;

import com.whosbean.newim.common.MessageUtil;
import com.whosbean.newim.entity.ExchangeMessage;
import com.whosbean.newim.gateway.GatewayConfig;
import com.whosbean.newim.gateway.connection.ChannelsHolder;
import com.whosbean.newim.service.ChatMessageService;
import com.whosbean.newim.service.ChatMessageServiceFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

/**
 * Created by yaming_deng on 14-9-9.
 */
public class MessageExchangeHandler extends SimpleChannelInboundHandler<byte[]> {

    protected static Logger logger = LoggerFactory.getLogger(MessageExchangeHandler.class);

    private ExecutorService executor = null;
    private ChatMessageService chatMessageService;

    public MessageExchangeHandler(){
        chatMessageService = ChatMessageServiceFactory.get(GatewayConfig.current);
        //executor = Executors.newFixedThreadPool(10);
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
        ExchangeMessage message = null;
        try {
            message = MessageUtil.asT(ExchangeMessage.class, (byte[])msg);
        } catch (IOException e) {
            logger.error("ExchangeMessage解析错误", e);
            ack(ctx.channel(), "ERR");
            return;
        }

        byte[] bytes = chatMessageService.getBytes(message.messageId);
        int total = 0;
        for (Integer cid : message.channelIds){
            Channel c = ChannelsHolder.get(cid);
            if (c != null){
                ChannelsHolder.ack(logger, c, bytes);
                total ++;
            }
        }

        ack(ctx.channel(), total + "");
    }

    private void ack(final Channel ctx, String msg) {
        ChannelsHolder.ack(logger, ctx, msg);
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
