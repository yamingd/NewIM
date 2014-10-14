package com.whosbean.newim.chatter;

import com.whosbean.newim.config.AbstractConfig;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by yaming_deng on 2014/9/28.
 */
@Component
public class ChatterConfig extends AbstractConfig {

    public static ChatterConfig current = null;

    private String host;
    private Integer port;
    private String sig;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        current = this;
        this.initGatewayInfo();
    }

    @Override
    public String getConfName() {
        return "chatter";
    }

    private void initGatewayInfo(){
        Map server = this.get(Map.class, "server");
        host = (String)server.get("ip");
        port = (Integer)server.get("port");
        sig = host+":"+ port;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getSig() {
        return sig;
    }
}
