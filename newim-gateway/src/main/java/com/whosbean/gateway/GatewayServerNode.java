package com.whosbean.gateway;

import com.whosbean.newim.server.ChatServerNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by yaming_deng on 14-9-9.
 */
@Component
public class GatewayServerNode extends ChatServerNode {

    public static final String ROLE_GATEWAY = "gateway";

    public static GatewayServerNode current = null;

    @Autowired
    private GatewayConfig gatewayConfig;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        current = this;
    }

    @Override
    protected String getRole() {
        return ROLE_GATEWAY;
    }

    @Override
    protected String getName() {
        return gatewayConfig.getGatewaySig();
    }

    @Override
    protected String getConf() {
        return getName();
    }
}
