package com.whosbean.newim.gateway.connection;

import com.whosbean.newim.common.MessageUtil;
import com.whosbean.newim.entity.ChatMessage;
import com.whosbean.newim.gateway.GatewayServerNode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yaming_deng on 14-9-9.
 */
public class ChannelsHolder {

    private static ConcurrentHashMap<Integer, Channel> mapping = new ConcurrentHashMap<Integer, Channel>(1024);

    /**
     * Add
     * @param c
     */
    public static void add(final Channel c){
        Channel exists = mapping.putIfAbsent(c.hashCode(), c);
        if (exists == null){

        }
    }

    /**
     *
     * Remove
     *
     * @param c
     * @return
     */
    public static boolean remove(final Channel c){
        boolean flag = mapping.remove(c.hashCode()) != null;
        return flag;
    }

    /**
     * Get
     *
     * @param id
     * @return
     */
    public static Channel get(final Integer id){
        return mapping.get(id);
    }

    public static void ack(final Logger logger, final Channel ctx, String msg) {
        //回复客户端.
        if (logger.isDebugEnabled()){
            logger.debug("ack: {}/{}", msg, ctx);
        }
        byte[] bytes = msg.getBytes();
        ack(logger, ctx, bytes, null);
    }

    public static void ack(final Logger logger, final Channel ctx, ChatMessage msg) throws IOException {
        //回复客户端.
        byte[] bytes = MessageUtil.asBytes(msg);
        ack(logger, ctx, bytes, null);
    }

    public static void ack(final Logger logger, final Channel ctx, byte[] bytes, final String chatPath) {
        if (ctx == null){
            logger.error("argument ctx is NULL. ");
            return;
        }
        if (bytes == null || bytes.length == 0){
            logger.error("argument bytes is NULL or size=0");
            return;
        }

        final ByteBuf data = Unpooled.copiedBuffer(bytes);
        final ChannelFuture cf = ctx.writeAndFlush(new BinaryWebSocketFrame(data));
        cf.addListener(new GenericFutureListener<Future<Void>>() {
            @Override
            public void operationComplete(Future<Void> future) throws Exception {
                if(future.cause() != null){
                    if (chatPath != null){
                        //remove this client from members.
                        GatewayServerNode.current.remConnection(chatPath, ctx.hashCode());
                    }
                    logger.error("发送消息错误. ctx=" + ctx, future.cause());
                    remove(ctx);
                    ctx.close();
                }else{
                    if (logger.isDebugEnabled()){
                        logger.debug("发送消息成功. ctx={}, chat={}", ctx, chatPath);
                    }
                }
            }
        });
    }
}
