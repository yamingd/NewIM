package com.whosbean.newim.chatter.router;

import com.whosbean.newim.chatter.RouterServerNode;
import com.whosbean.newim.zookeeper.ZKPaths;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by Yaming on 2014/10/17.
 */
public class BoxMsgListenThread extends Thread implements CuratorWatcher {

    protected Logger logger = null;

    private String boxid;
    private RouterServerNode routerServerNode;
    private ThreadPoolTaskExecutor executors;
    private String path;

    public BoxMsgListenThread(ThreadPoolTaskExecutor executors, String boxid) {
        this.boxid = boxid;
        this.routerServerNode = RouterServerNode.current;
        this.executors = executors;
        this.logger = LoggerFactory.getLogger(this.getClass().getName() + "." + boxid);
    }

    @Override
    public void run() {
        this.path = ZKPaths.NS_ROOT + ZKPaths.PATH_INBOX + "/" + boxid;

        try {
            hook(path);
        } catch (Exception e) {
            e.printStackTrace();
        }


        while (true){
            try {
                Thread.sleep(3600 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void hook(final String path) throws Exception {
        Stat stat = routerServerNode.getZkClient()
                .checkExists().forPath(path);
        if (stat == null){
            routerServerNode.getZkClient().create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(path);
        }

        routerServerNode.getZkClient()
                .getChildren().usingWatcher(this).forPath(path);
    }

    @Override
    public void process(WatchedEvent event) throws Exception {
        logger.info("process Event: {}", event);
        if(event.getType() == Watcher.Event.EventType.NodeDataChanged){
            String data = getData(event.getPath());
            logger.info(path + ":" + data);
        }else if(event.getType() == Watcher.Event.EventType.NodeChildrenChanged){
            List<String> childs = routerServerNode.getZkClient()
                    .getChildren().usingWatcher(this).forPath(path);
            logger.info("Found Message. total={}", childs.size());
            for(String seqid : childs){
                executors.submit(new NewMessageNotifyThread(routerServerNode, boxid, seqid));
            }
        }
    }

    public String getData(String path) throws Exception {
        byte[] bytes = routerServerNode.getZkClient().getData().forPath(path);
        return new String(bytes, Charset.forName("utf-8"));
    }
}
