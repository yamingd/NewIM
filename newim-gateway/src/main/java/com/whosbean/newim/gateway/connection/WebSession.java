package com.whosbean.newim.gateway.connection;

import java.io.Serializable;

/**
 * Created by yaming_deng on 14-9-9.
 */
public class WebSession implements Serializable {

    private String uid;

    public WebSession(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
