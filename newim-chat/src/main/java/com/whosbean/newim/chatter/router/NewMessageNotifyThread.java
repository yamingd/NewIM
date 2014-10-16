package com.whosbean.newim.chatter.router;

import com.google.common.collect.Lists;
import com.whosbean.newim.chatter.RouterServerNode;
import com.whosbean.newim.chatter.exchange.ExchangeClient;
import com.whosbean.newim.chatter.exchange.ExchangeClientManager;
import com.whosbean.newim.entity.ExchangeMessage;
import com.whosbean.newim.zookeeper.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Yaming on 2014/10/14.
 */
public class NewMessageNotifyThread implements Runnable {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private RouterServerNode routerServerNode;
    private String boxid;
    private String[] data;

    public NewMessageNotifyThread(RouterServerNode routerServerNode, String boxid, String[] data) {
        this.routerServerNode = routerServerNode;
        this.boxid = boxid;
        this.data = data;
    }

    @Override
    public void run() {
        String path = String.format("%s/%s/members/%s", ZKPaths.NS_ROOT, ZKPaths.PATH_CHATS, boxid);
        String msgId = this.data[this.data.length-1];
        try {
            List<String> members = this.routerServerNode.getZkClient().getChildren().forPath(path);
            //TODO:too many members then split
            ExchangeMessage message = new ExchangeMessage();
            message.chatRoomId = this.boxid;
            for(String host : members){
                ExchangeClient client = ExchangeClientManager.instance.find(host);
                message.messageId = msgId;
                message.channelIds = Lists.newArrayList();
                List<String> channels = this.routerServerNode.getZkClient().getChildren().forPath(path + "/" + host);
                for(String cid : channels){
                    message.channelIds.add(new Integer(cid));
                }
                message.chatPath = path + "/" + host;
                logger.info("ExchangeClient send message. " + message);
                client.send(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
