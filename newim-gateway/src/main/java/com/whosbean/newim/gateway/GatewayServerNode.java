package com.whosbean.newim.gateway;

import com.whosbean.newim.server.ChatServerNode;
import com.whosbean.newim.server.ServerNodeRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by yaming_deng on 14-9-9.
 */
@Component
public class GatewayServerNode extends ChatServerNode {

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
        return ServerNodeRoles.ROLE_EXCHANGE;
    }

    @Override
    protected String getName() {
        return gatewayConfig.getExchangeSig();
    }

    @Override
    protected String getConf() {
        return gatewayConfig.getGatewaySig();
    }
}
