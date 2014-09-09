package com.whosbean.gateway.connection;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;

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
            //TODO:new one then add it into Zookeeper
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
        if (flag){
            //TODO:remove it from zookeeper too.
        }
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
        byte[] bytes = msg.getBytes();
        final ByteBuf data = ctx.alloc().buffer(bytes.length); // (2)
        data.writeBytes(bytes);
        final ChannelFuture cf = ctx.writeAndFlush(data);
        cf.addListener(new GenericFutureListener<Future<Void>>() {
            @Override
            public void operationComplete(Future<Void> future) throws Exception {
                if(future.cause() != null){
                    logger.error("发送消息错误. cid=" + ctx.hashCode(), future.cause());
                    remove(ctx);
                    ctx.close();
                }
            }
        });
    }
}
