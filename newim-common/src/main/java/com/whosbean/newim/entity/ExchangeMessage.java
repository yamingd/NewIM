package com.whosbean.newim.entity;

import com.google.common.collect.Lists;
import org.msgpack.annotation.Message;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yaming_deng on 14-9-9.
 */
@Message
public class ExchangeMessage implements Serializable {
    /**
     * those clients will be receiving this messages
     * in batch mode
     */
    public List<Integer> channelIds;
    /**
     * message id
     */
    public String messageId;
    /**
     * message content
     */
    public String message;

    public ExchangeMessage() {
        channelIds = Lists.newArrayList();
    }

    @Override
    public String toString() {
        return "ExchangeMessage{" +
                "channelIds=" + channelIds +
                ", messageId='" + messageId + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
