package com.whosbean.newim.chatter.router;

import com.whosbean.newim.chatter.RouterServerNode;
import com.whosbean.newim.chatter.exchange.ExchangeClientManager;
import com.whosbean.newim.zookeeper.ZKPaths;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Yaming on 2014/10/14.
 */
@Component
public class ChatMessageListener implements InitializingBean, DisposableBean {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public static ChatMessageListener current;

    @Autowired
    private RouterServerNode routerServerNode;

    @Autowired
    private ExchangeClientManager exchangeClientManager;

    private ThreadPoolTaskExecutor executors = null;
    private ConcurrentHashMap<String, Integer> boxMap = new ConcurrentHashMap<String, Integer>();

    private volatile boolean stopped = false;

    @Override
    public void destroy() throws Exception {
        stopped = true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        int limit = Runtime.getRuntime().availableProcessors() * 2;
        executors = new ThreadPoolTaskExecutor();
        executors.setCorePoolSize(limit/5);
        executors.setMaxPoolSize(limit);
        executors.setWaitForTasksToCompleteOnShutdown(true);
        executors.afterPropertiesSet();
        new ChatListenThread().start();
        current = this;
        logger.info("ChatMessageListner start.");
    }

    public String getData(String path) throws Exception {
        byte[] bytes = routerServerNode.getZkClient().getData().forPath(path);
        return new String(bytes, Charset.forName("utf-8"));
    }

    public void remove(String boxid){
        boxMap.remove(boxid);
        logger.info("Box Listener quit. " + boxid);
    }

    public class NewMemberWatcher implements CuratorWatcher {

        private final String path;

        public String getPath() {
            return path;
        }

        public NewMemberWatcher(String path) {
            this.path = path;
        }

        @Override
        public void process(WatchedEvent event) throws Exception {
            logger.info("process Event: {}", event);
            if(event.getType() == Watcher.Event.EventType.NodeDataChanged){
                String data = getData(event.getPath());
                logger.info(path + ":" + data);
            }else if(event.getType() == Watcher.Event.EventType.NodeChildrenChanged){
                List<String> childs = routerServerNode.getZkClient().getChildren().forPath(path);
                for(String boxid : childs){
                    if (!boxMap.contains(boxid)){
                        boxMap.put(boxid, 1);
                        new BoxMsgListenThread(executors, boxid).start();
                    }
                }
            }
        }

    }

    public class ChatListenThread extends Thread{

        @Override
        public void run() {
            String path = ZKPaths.NS_ROOT + ZKPaths.PATH_CHATS;
            List<String> childs = null;
            while (childs == null){
                try {
                    childs = routerServerNode.getZkClient()
                            .getChildren()
                            .usingWatcher(new NewMemberWatcher(path))
                            .forPath(path);
                    if (childs != null){
                        logger.info("Found path. {}, size {}", path, childs.size());
                        if (childs.size() > 0){
                            //start boxMessageListen
                            for(String boxid : childs){
                                boxMap.put(boxid, 1);
                                new BoxMsgListenThread(executors, boxid).start();
                            }
                        }
                        break;
                    }else{
                        logger.info("Wait for path. " + path);
                        Thread.sleep(1 * 1000);
                    }
                } catch (Exception e) {
                    if (e instanceof KeeperException){
                        KeeperException ex = (KeeperException)e;
                        if (ex.code().equals(KeeperException.Code.NONODE)){
                            try {
                                routerServerNode.getZkClient().create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                            } catch (Exception e1) {
                                logger.error(e1.getMessage(), e1);
                            }
                        }
                    }else {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

}
