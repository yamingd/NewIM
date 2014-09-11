package com.whosbean.newim.entity;

import org.msgpack.annotation.Message;

import java.util.UUID;

/**
 * Created by yaming_deng on 14-9-9.
 */
@Message
public class ChatMessage extends Chatbox {

    public static final Integer OP_JOIN = 1;
    public static final Integer OP_QUIT = 2;
    public static final Integer OP_CHAT = 3;

    public String uuid;
    public String sender;
    public String receiver;
    public Integer type;
    public String body;
    public Integer op;

    public void assignUuid(){
        this.uuid = UUID.randomUUID().toString();
        this.uuid = this.uuid.replace("-", "");
    }

}
