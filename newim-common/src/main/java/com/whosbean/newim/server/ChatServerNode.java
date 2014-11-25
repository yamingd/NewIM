package com.whosbean.newim.server;

import com.whosbean.newim.entity.ChatMessage;
import com.whosbean.newim.zookeeper.ZKPaths;
import io.netty.channel.Channel;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import java.util.List;

/**
 * Created by yaming_deng on 14-9-9.
 */
public class ChatServerNode extends ServerNode {

    /**
     * 写入新消息到Zookeeper. Router会得到通知，并读取消息，路由给多个客户端.
     * @param chatMessage
     * @throws Exception
     */
    public void newMessage(final ChatMessage chatMessage) throws Exception {
        //notify exchange a new-added message
        if (this.client == null){
            logger.error("Zookeeper Client is Lost");
            return;
        }

        String path = ZKPaths.getInboxPath(chatMessage.getBoxid());
        Stat stat = this.client.checkExists().forPath(path);
        if (stat == null){
            this.client.create().creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT).forPath(path);
        }

        String data = chatMessage.getUuid();
        if (logger.isDebugEnabled()){
            logger.debug("newMessage. path0={}", path);
        }
        if (data == null){
            data = "NULL";
        }
        path = path + "/" + chatMessage.getBoxid() + "-";
        if (logger.isDebugEnabled()){
            logger.debug("newMessage. path1={}", path);
        }
        this.client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                .inBackground().forPath(path, data.getBytes("UTF-8"));
    }

    public void outMessage(final String msgPath) throws Exception {
        //notify exchange a new-added message
        if (this.client == null){
            logger.error("Zookeeper Client is Lost");
            return;
        }
        if (logger.isDebugEnabled()){
            logger.debug("outMessage. path=" + msgPath);
        }
        byte[] data;
        try {
            data = this.client.getData().forPath(msgPath);
        } catch (KeeperException e) {
            if (e.code().equals(KeeperException.Code.NONODE)){
                this.delete(msgPath);
                return;
            }else{
                throw e;
            }
        }
        String path1 = msgPath.replace("/inbox/", "/outbox/");

        this.client.create().creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT).forPath(path1, data);
        this.delete(msgPath);
    }

    /**
     * 当客户端断开连接后，把链接从chat room中移除.
     * @param chatPath
     * @throws Exception
     */
    public void remConnection(String chatPath, Integer channelId) throws Exception {
        //lost a connection
        //a member quit from a chat
        if (this.client == null){
            logger.error("Zookeeper Client is Lost");
            return;
        }
        String path = ZKPaths.getMemberPath(chatPath, channelId);
        if (logger.isDebugEnabled()){
            logger.debug("remConnection. path=" + path);
        }
        this.delete(path);
    }

    /**
     * 加入chat room
     * @param channel
     * @param message
     * @throws Exception
     */
    public void join(final Channel channel, final ChatMessage message) throws Exception {
        //new member join a chat
        if (this.client == null){
            logger.error("Zookeeper Client is Lost");
            return;
        }
        String path = ZKPaths.getMemberPath(message.getBoxid(), this.getName(), channel.hashCode());
        if (logger.isDebugEnabled()){
            logger.debug("join. path={}", path);
        }
        String data = message.getSender();
        this.client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).inBackground().forPath(path, data.getBytes("UTF-8"));
    }

    public List<String> getMembers(final String boxid) throws Exception {
        if (this.client == null){
            logger.error("Zookeeper Client is Lost");
            return null;
        }
        String path = ZKPaths.getMemberPath(boxid);
        if (logger.isDebugEnabled()){
            logger.debug("getMembers. path={}", path);
        }
        List<String> list = this.client.getChildren().forPath(path);
        return list;
    }

    /**
     * 退出chat room
     * @param channel
     * @param message
     * @throws Exception
     */
    public void quit(final Channel channel, final ChatMessage message) throws Exception {
        //a member quit from a chat
        if (this.client == null){
            logger.error("Zookeeper Client is Lost");
            return;
        }
        String path = ZKPaths.getMemberPath(message.getBoxid(), this.getName(), channel.hashCode());
        if (logger.isDebugEnabled()){
            logger.debug("quit. path={}", path);
        }
        this.delete(path);
    }

    public void delete(String path){
        try {
            this.client.delete().inBackground().forPath(path);
        }catch (KeeperException e){
            if (e.code().equals(KeeperException.Code.NONODE)){

            }else{
                logger.error(e.getMessage(), e);
            }
        }catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
