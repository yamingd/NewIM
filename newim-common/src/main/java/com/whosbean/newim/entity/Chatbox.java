package com.whosbean.newim.entity;

import org.msgpack.annotation.Message;

/**
 * Created by yaming_deng on 14-9-9.
 */
@Message
public class Chatbox {

    private Integer id;
    private Integer group = 0;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGroup() {
        return group;
    }

    public void setGroup(Integer group) {
        this.group = group;
    }
}
