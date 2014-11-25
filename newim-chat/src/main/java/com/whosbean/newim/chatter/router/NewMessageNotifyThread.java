package com.whosbean.newim.chatter.router;

import com.whosbean.newim.chatter.RouterServerNode;
import com.whosbean.newim.chatter.exchange.ExchangeClient;
import com.whosbean.newim.chatter.exchange.ExchangeClientManager;
import com.whosbean.newim.entity.ExchangeMessage;
import com.whosbean.newim.zookeeper.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by Yaming on 2014/10/14.
 */
public class NewMessageNotifyThread implements Runnable {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private RouterServerNode routerServerNode;
    private String boxid;
    private String seqid;


    public NewMessageNotifyThread(RouterServerNode routerServerNode, String boxid, String seqid) {
        this.routerServerNode = routerServerNode;
        this.boxid = boxid;
        this.seqid = seqid;
    }

    @Override
    public void run() {
        String msgpath = ZKPaths.getInboxPath(boxid, seqid);
        String path = ZKPaths.getMemberPath(boxid);
        try {
            String msgid = this.getData(msgpath);
            List<String> members = this.routerServerNode.getZkClient().getChildren().forPath(path);
            //TODO:too many members then split
            ExchangeMessage.Builder message = ExchangeMessage.newBuilder();
            message.setChatRoomId(this.boxid);
            message.setMsgPath(msgpath);
            for(String host : members){
                ExchangeClient client = ExchangeClientManager.instance.find(host);
                message.setMessageId(msgid);
                List<String> channels = this.routerServerNode.getZkClient().getChildren().forPath(path + "/" + host);
                for(String cid : channels){
                    message.addChannelId(new Integer(cid));
                }
                message.setChatPath(boxid + "/" + host);
                logger.info("ExchangeClient send message. " + message);
                client.send(message.build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getData(String path) throws Exception {
        byte[] bytes = routerServerNode.getZkClient().getData().forPath(path);
        return new String(bytes, Charset.forName("utf-8"));
    }
}
