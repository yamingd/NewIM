package com.whosbean.newim.server;

import org.springframework.beans.factory.InitializingBean;

/**
 * Created by yaming_deng on 14-9-9.
 */
public abstract class ServerNode implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        this.connectZookeeper();
        this.registryAtZookeeper();
    }

    protected void connectZookeeper(){

    }

    protected void registryAtZookeeper(){
        //ip+":"+port
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
