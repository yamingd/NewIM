package com.whosbean.newim.server;

import com.whosbean.newim.entity.ChatMessage;
import io.netty.channel.Channel;

/**
 * Created by yaming_deng on 14-9-9.
 */
public class ChatServerNode extends ServerNode {

    public void newMessage(final Channel channel, final ChatMessage chatMessage){

    }

    public void addConnection(final Channel channel){

    }

    public void remConnection(final Channel channel){

    }

    public void join(final Channel channel, final ChatMessage chatbox){

    }

    public void quit(final Channel channel, final ChatMessage chatbox){

    }

}
