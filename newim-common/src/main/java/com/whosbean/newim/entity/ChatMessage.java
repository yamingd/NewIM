package com.whosbean.newim.entity;

import org.msgpack.annotation.Message;

import java.util.UUID;

/**
 * Created by yaming_deng on 14-9-9.
 */
@Message
public class ChatMessage extends Chatbox {

    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIDEO = 2;
    public static final int TYPE_AUDIO = 3;
    public static final int TYPE_FILE = 4;

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

    public ChatMessage assignUuid(){
        this.uuid = UUID.randomUUID().toString();
        this.uuid = this.uuid.replace("-", "");
        return this;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "uuid='" + uuid + '\'' +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", type=" + type +
                ", body='" + body + '\'' +
                '}';
    }

    public static void main(String[] args) {
        System.out.println(new ChatMessage().assignUuid().uuid);
    }

}
