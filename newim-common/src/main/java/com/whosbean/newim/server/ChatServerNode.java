package com.whosbean.newim.server;

import com.whosbean.newim.entity.ChatMessage;
import com.whosbean.newim.zookeeper.ZKPaths;
import io.netty.channel.Channel;

import java.util.List;

/**
 * Created by yaming_deng on 14-9-9.
 */
public class ChatServerNode extends ServerNode {

    /**
     * 写入新消息到Zookeeper. Router会得到通知，并读取消息，路由给多个客户端.
     * @param channel
     * @param chatMessage
     * @throws Exception
     */
    public void newMessage(final Channel channel, final ChatMessage chatMessage) throws Exception {
        //notify exchange a new-added message
        if (this.client == null){
            logger.error("Zookeeper Client is Lost");
            return;
        }
        String path = ZKPaths.getMessagePath(chatMessage.id, chatMessage.uuid);
        String data = "NM"+"\n"+chatMessage.uuid;
        this.client.create().forPath(path, data.getBytes("UTF-8"));
    }

    /**
     * 当Router路由成功消息后. 会把消息删除.
     * @param channel
     * @param msgPath
     * @throws Exception
     */
    public void remMessage(final Channel channel, final String msgPath) throws Exception {
        //notify exchange a new-added message
        if (this.client == null){
            logger.error("Zookeeper Client is Lost");
            return;
        }
        String path = ZKPaths.getMessagePath(msgPath);
        this.client.delete().forPath(path);
    }

    /**
     * 当客户端断开连接后，把链接从chat room中移除.
     * @param channel
     * @param chatPath
     * @throws Exception
     */
    public void remConnection(final Channel channel, String chatPath) throws Exception {
        //lost a connection
        //a member quit from a chat
        if (this.client == null){
            logger.error("Zookeeper Client is Lost");
            return;
        }
        String path = ZKPaths.getMemberPath(chatPath, channel.hashCode());
        this.client.delete().forPath(path);
    }

    /**
     * 加入chat room
     * @param channel
     * @param chatbox
     * @throws Exception
     */
    public void join(final Channel channel, final ChatMessage chatbox) throws Exception {
        //new member join a chat
        if (this.client == null){
            logger.error("Zookeeper Client is Lost");
            return;
        }
        String path = ZKPaths.getMemberPath(chatbox.id, this.getName(), channel.hashCode());
        String data = "NJ"+"\n"+chatbox.uuid;
        this.client.create().forPath(path, data.getBytes("UTF-8"));
    }

    public List<String> getMembers(final String boxid) throws Exception {
        if (this.client == null){
            logger.error("Zookeeper Client is Lost");
            return null;
        }
        String path = ZKPaths.getMemberPath(boxid);
        List<String> list = this.client.getChildren().forPath(path);
        return list;
    }

    /**
     * 退出chat room
     * @param channel
     * @param chatbox
     * @throws Exception
     */
    public void quit(final Channel channel, final ChatMessage chatbox) throws Exception {
        //a member quit from a chat
        if (this.client == null){
            logger.error("Zookeeper Client is Lost");
            return;
        }
        String path = ZKPaths.getMemberPath(chatbox.id, this.getName(), channel.hashCode());
        this.client.delete().forPath(path);
    }

}
