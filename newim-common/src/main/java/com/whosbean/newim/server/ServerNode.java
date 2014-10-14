package com.whosbean.newim.server;

import com.whosbean.newim.zookeeper.ZKPaths;
import com.whosbean.newim.zookeeper.ZookeeperConfig;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by yaming_deng on 14-9-9.
 */
public abstract class ServerNode implements InitializingBean, DisposableBean {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected ZookeeperConfig zookeeperConfig;

    protected CuratorFramework client;

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("Connecting to Zookeeper. " + zookeeperConfig.getServers());
        this.connectZookeeper();
        logger.info("Registry this server to Zookeeper.");
        this.registryAtZookeeper();
        logger.info("Startup Done. sig = " + this.getName());
    }

    @Override
    public void destroy() throws Exception {
        if (client != null){
            client.close();
        }
    }

    protected void connectZookeeper(){
        String zookeeperConnectionString = zookeeperConfig.getServers(); //"localhost:2181,localhost:2182,localhost:2183";
        int limit = zookeeperConfig.getRetryLimit();
        int wait = zookeeperConfig.getRetryInterval();
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(wait, limit);
        client = CuratorFrameworkFactory.newClient(zookeeperConnectionString, retryPolicy);
        client.start();
        client.newNamespaceAwareEnsurePath(ZKPaths.NS_ROOT);
    }

    protected void registryAtZookeeper() throws Exception {
        //ip+":"+port
        String path = String.format("%s/%s/%s", ZKPaths.PATH_SERVERS, this.getRole(), this.getName());
        try {
            Stat stat = client.checkExists().forPath(path);
            if (stat != null) {
                logger.info("***** node [" + path + "] existed!");
            }else {
                byte[] data = this.getConf().getBytes("UTF-8");
                client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, data);
            }
        } catch (Exception e) {
            logger.error("registryAtZookeeper Error. sig=" + this.getName(), e);
            throw e;
        }
    }

    public List<String> getServers(String role) throws Exception {
        String path = String.format("%s/%s", ZKPaths.PATH_SERVERS, role);
        return client.getChildren().forPath(path);
    }

    public CuratorFramework getZkClient(){
        return client;
    }

    protected String getRole(){
        return null;
    }

    protected String getName(){
        return null;
    }

    protected String getConf(){
        return null;
    }
}
