package com.whosbean.newim.entity;

import org.msgpack.annotation.Message;

import java.util.UUID;

/**
 * Created by yaming_deng on 14-9-9.
 */
@Message
public class ChatMessage extends Chatbox {
    /**
     * 消息id
     */
    public String uuid;
    /**
     * 发送人
     */
    public String sender;
    /**
     * 接收人
     */
    public String receiver;
    /**
     * 消息类型
     */
    public Integer type;
    /**
     * 消息主体
     */
    public String body;

    public void assignUuid(){
        this.uuid = UUID.randomUUID().toString();
        this.uuid = this.uuid.replace("-", "");
    }

}
