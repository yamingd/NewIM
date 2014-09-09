package com.whosbean.newim.gateway.handler;

import com.whosbean.newim.gateway.connection.WebSession;
import io.netty.util.AttributeKey;

/**
 * Created by yaming_deng on 14-9-9.
 */
public class ChannelAttributes {

    public static final AttributeKey<WebSession> SESSIOON_ATTR_KEY =
            AttributeKey.valueOf(ChannelAttributes.class.getName() + ".Session");

}
