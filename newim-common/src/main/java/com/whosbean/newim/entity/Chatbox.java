package com.whosbean.newim.entity;

import org.msgpack.annotation.Message;

/**
 * Created by yaming_deng on 14-9-9.
 */
@Message
public class Chatbox {
    /**
     * chat room id.
     */
    public Integer id;
    /**
     * if this chat room is a group chat then set 1 else set 0.
     */
    public Integer group = 0;
}
