package com.whosbean.newim.zookeeper;

import com.whosbean.newim.config.AbstractConfig;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by yaming_deng on 14-9-5.
 */
@Component
public class ZookeeperConfig extends AbstractConfig {

    public static final String CONF_ZOOKEEPER = "zookeeper";

    private Map<String, Integer> retrys;

    @Override
    public String getConfName() {
        return CONF_ZOOKEEPER;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.cfgFile = CONF_ZOOKEEPER;
        super.afterPropertiesSet();
        this.retrys = this.get(Map.class, "retry");
    }

    /**
     String zookeeperConnectionString = "localhost:2181,localhost:2182,localhost:2183";
     RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
     CuratorFramework client = CuratorFrameworkFactory.newClient(
     zookeeperConnectionString, retryPolicy);
     client.start();
     */

    public String getServers(){
        String servs = this.get(String.class, "servers");
        return servs;
    }

    public Integer getRetryInterval(){
        Integer r = this.retrys.get("interval");
        return r;
    }

    public Integer getRetryLimit(){
        Integer r = this.retrys.get("limit");
        return r;
    }
}
