package com.whosbean.newim.chatter.router;

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
public class NewMemberNotifyThread implements Runnable {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private RouterServerNode routerServerNode;
    private String boxid;
    private String[] data;

    public NewMemberNotifyThread(RouterServerNode routerServerNode, String boxid, String[] data) {
        this.routerServerNode = routerServerNode;
        this.boxid = boxid;
        this.data = data;
    }

    @Override
    public void run() {
        String path = ZKPaths.getMemberPath(boxid);
        String msgId = this.data[this.data.length-1];
        try {
            List<String> members = this.routerServerNode.getZkClient().getChildren().forPath(path);
            //TODO:too many members then split
            ExchangeMessage.Builder message = ExchangeMessage.newBuilder();
            message.setChatRoomId(this.boxid);
            for(String host : members){
                ExchangeClient client = ExchangeClientManager.instance.find(host);
                message.setMessageId(msgId);
                List<String> channels = this.routerServerNode.getZkClient().getChildren().forPath(path + "/" + host);
                for(String cid : channels){
                    message.addChannelId(new Integer(cid));
                }
                message.setChatPath(path + "/" + host);
                logger.info("ExchangeClient send message. " + message);
                client.send(message.build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
