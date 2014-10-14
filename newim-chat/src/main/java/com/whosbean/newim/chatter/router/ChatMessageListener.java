package com.whosbean.newim.chatter.router;

import com.whosbean.newim.chatter.RouterServerNode;
import com.whosbean.newim.chatter.exchange.ExchangeClientManager;
import com.whosbean.newim.zookeeper.ZKPaths;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Yaming on 2014/10/14.
 */
@Component
public class ChatMessageListener implements InitializingBean, DisposableBean {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RouterServerNode routerServerNode;

    @Autowired
    private ExchangeClientManager exchangeClientManager;

    private ExecutorService executors = null;

    @Override
    public void destroy() throws Exception {

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        int pool = Runtime.getRuntime().availableProcessors() * 2;
        executors = Executors.newFixedThreadPool(pool);
        new ListenThread().start();
        logger.info("ChatMessageListner start.");
    }

    public String getData(String path) throws Exception {
        byte[] bytes = routerServerNode.getZkClient().getData().forPath(path);
        return new String(bytes, Charset.forName("utf-8"));
    }

    public class ServerNodeWatcher implements CuratorWatcher {

        private final String path;

        public String getPath() {
            return path;
        }

        public ServerNodeWatcher(String path) {
            this.path = path;
        }

        @Override
        public void process(WatchedEvent event) throws Exception {
            logger.info("Event: {}", event.getType());
            if(event.getType() == Watcher.Event.EventType.NodeDataChanged){
                String data = getData(event.getPath());
                logger.info(path + ":" + data);
            }else if(event.getType() == Watcher.Event.EventType.NodeChildrenChanged){
                String changedPath = event.getPath().replace(ZKPaths.NS_ROOT, "").replace(this.path, "");
                logger.info("Changed Path: {}", changedPath);
                String[] temp = changedPath.split("/");
                String chatBoxId = temp[temp.length-2];
                String data = getData(event.getPath());
                temp = data.split("\n");
                logger.info("Message: {}, {}", chatBoxId, data);
                if (temp[0].equalsIgnoreCase("NJ")){
                    executors.submit(new NewMemberNotifyThread(routerServerNode, chatBoxId, temp));
                }else if(temp[0].equalsIgnoreCase("NM")){
                    executors.submit(new NewMessageNotifyThread(routerServerNode, chatBoxId, temp));
                }
            }
        }

    }

    public class ListenThread extends Thread{

        @Override
        public void run() {
            Stat stat = null;
            while (stat == null){
                try {
                    stat = routerServerNode.getZkClient()
                            .checkExists()
                            .usingWatcher(new ServerNodeWatcher(ZKPaths.PATH_CHATS))
                            .forPath(ZKPaths.PATH_CHATS);
                    if (stat != null){
                        logger.info("Found path. " + ZKPaths.PATH_CHATS);
                        break;
                    }else{
                        logger.info("Wait for path. " + ZKPaths.PATH_CHATS);
                        Thread.sleep(1 * 1000);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }
}
