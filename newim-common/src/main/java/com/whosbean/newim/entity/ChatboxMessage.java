package com.whosbean.newim.entity;

import org.msgpack.annotation.Message;

/**
 * Created by yaming_deng on 14-9-9.
 */
@Message
public class ChatboxMessage extends Chatbox {

    public static final Integer OP_JOIN = 1;
    public static final Integer OP_QUIT = 2;
    public static final Integer OP_LOST = 3;

    private String sender;
    private Integer op;

    public Integer getOp() {
        return op;
    }

    public void setOp(Integer op) {
        this.op = op;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
