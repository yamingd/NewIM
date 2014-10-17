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
     * /newim/chats/0000x/channel-id(data=ip:port-for-exchange)
     */
    public static final String PATH_CHATS = "/chats";
    /**
     * new message
     * /newim/inbox/{boxid}/{msgid}
     */
    public static final String PATH_INBOX = "/inbox";
    /**
     * message out
     * /newim/outbox/{boxid}/{msgid}
     */
    public static final String PATH_OUTBOX = "/outbox";

    public static String getServerPath(String role){
        String path = String.format("%s%s/%s", ZKPaths.NS_ROOT, ZKPaths.PATH_SERVERS, role);
        return path;
    }

    public static String getServerPath(String role, String name){
        String path = String.format("%s%s/%s/%s", ZKPaths.NS_ROOT, ZKPaths.PATH_SERVERS, role, name);
        return path;
    }

    public static String getMemberPath(String boxid, String serverName, Integer channelid){
        String path = String.format("%s%s/%s/%s/%s", ZKPaths.NS_ROOT, ZKPaths.PATH_CHATS, boxid, serverName, channelid);
        return path;
    }

    public static String getMemberPath(String chatPath, Integer channelid){
        String path = String.format("%s%s/%s/%s", ZKPaths.NS_ROOT, ZKPaths.PATH_CHATS, chatPath, channelid);
        return path;
    }

    public static String getMemberPath(String boxid){
        String path = String.format("%s%s/%s", ZKPaths.NS_ROOT, ZKPaths.PATH_CHATS, boxid);
        return path;
    }

    public static String getInboxPath(String boxid, String msgid){
        String path = String.format("%s%s/%s/%s", ZKPaths.NS_ROOT, ZKPaths.PATH_INBOX, boxid, msgid);
        return path;
    }

    public static String getOutboxPath(String boxid, String msgid){
        String path = String.format("%s%s/%s/%s", ZKPaths.NS_ROOT, ZKPaths.PATH_OUTBOX, boxid, msgid);
        return path;
    }

    public static String getInboxPath(String msgpath){
        String path = String.format("%s%s/%s", ZKPaths.NS_ROOT, ZKPaths.PATH_INBOX, msgpath);
        return path;
    }

    public static String getOutboxPath(String msgpath){
        String path = String.format("%s%s/%s", ZKPaths.NS_ROOT, ZKPaths.PATH_OUTBOX, msgpath);
        return path;
    }
}
