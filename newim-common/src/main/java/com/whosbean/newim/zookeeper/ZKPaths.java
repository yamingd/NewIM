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

    public static String getServerPath(String role){
        String path = String.format("%s%s/%s", ZKPaths.NS_ROOT, ZKPaths.PATH_SERVERS, role);
        return path;
    }

    public static String getServerPath(String role, String name){
        String path = String.format("%s%s/%s/%s", ZKPaths.NS_ROOT, ZKPaths.PATH_SERVERS, role, name);
        return path;
    }

    public static String getMemberPath(String boxid, String serverName, Integer channelid){
        String path = String.format("%s/%s/members/%s/%s/%s", ZKPaths.NS_ROOT, ZKPaths.PATH_CHATS, boxid, serverName, channelid);
        return path;
    }

    public static String getMemberPath(String chatPath, Integer channelid){
        String path = String.format("%s/%s/members/%s/%s", ZKPaths.NS_ROOT, ZKPaths.PATH_CHATS, chatPath, channelid);
        return path;
    }

    public static String getMemberPath(String boxid){
        String path = String.format("%s/%s/members/%s", ZKPaths.NS_ROOT, ZKPaths.PATH_CHATS, boxid);
        return path;
    }

    public static String getMessagePath(String boxid, String msgid){
        String path = String.format("%s/%s/messages/%s/%s", ZKPaths.NS_ROOT, ZKPaths.PATH_CHATS, boxid, msgid);
        return path;
    }

    public static String getMessagePath(String msgpath){
        String path = String.format("%s/%s/messages/%s", ZKPaths.NS_ROOT, ZKPaths.PATH_CHATS, msgpath);
        return path;
    }
}
