package com.whosbean.newim.chatter;

import com.whosbean.newim.server.ServerNode;
import com.whosbean.newim.server.ServerNodeRoles;
import org.springframework.stereotype.Component;

/**
 * Created by yaming_deng on 14-9-9.
 */
@Component
public class RouterServerNode extends ServerNode {

    public static RouterServerNode current = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        current = this;
    }

    @Override
    protected String getRole() {
        return ServerNodeRoles.ROLE_ROUTER;
    }

    @Override
    protected String getName() {
        return "router";
    }

    @Override
    protected String getConf() {
        return getName();
    }

}
