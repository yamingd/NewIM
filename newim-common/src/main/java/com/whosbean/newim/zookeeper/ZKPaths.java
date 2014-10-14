package com.whosbean.newim.zookeeper;

/**
 * Created by yaming_deng on 14-9-11.
 */
public class ZKPaths {
    /**
     * ROOT
     */
    public static final String NS_ROOT = "/newim";
    /**
     * /newim/servers/exchange/ip:port-for-exchange(data=ip:port-for-gateway)
     * /newim/servers/routers/ip:port(data=ip:port)
     */
    public static final String PATH_SERVERS = "/servers";
    /**
     * /newim/chats/members/0000x/channel-id(data=ip:port-for-exchange)
     * /newim/chats/messages/0000x/m-00xxxx(data=msg-id)
     */
    public static final String PATH_CHATS = "/chats";
}
