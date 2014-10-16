package com.whosbean.newim.entity;

import org.msgpack.annotation.Message;

/**
 * Created by yaming_deng on 14-9-9.
 */
@Message
public class Chatbox {

    public static final Integer OP_JOIN = 1;
    public static final Integer OP_QUIT = 2;
    public static final Integer OP_CHAT = 3;

    /**
     * chat room id.
     */
    public String id;
    /**
     * if this chat room is a group chat then set 1 else set 0.
     */
    public Integer group = 0;
    /**
     * 聊天室操作代号
     */
    public Integer op;
}
