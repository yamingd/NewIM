package com.whosbean.newim.entity;

import org.msgpack.annotation.Message;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yaming_deng on 14-9-9.
 */
@Message
public class ExchangeMessage implements Serializable {

    private List<Integer> channelIds;
    private String messageId;
    private String message;

    public List<Integer> getChannelIds() {
        return channelIds;
    }

    public void setChannelIds(List<Integer> channelIds) {
        this.channelIds = channelIds;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
