package com.whosbean.newim.server;

import com.whosbean.newim.entity.ChatMessage;
import io.netty.channel.Channel;

/**
 * Created by yaming_deng on 14-9-9.
 */
public class ChatServerNode extends ServerNode {

    public void newMessage(final Channel channel, final ChatMessage chatMessage){
        //TODO: notify exchange a new-added message
    }

    public void addConnection(final Channel channel){
        //TODO: new connection comming
    }

    public void remConnection(final Channel channel){
        //TODO: lost a connection
    }

    public void join(final Channel channel, final ChatMessage chatbox){
        //TODO: new member join a group
    }

    public void quit(final Channel channel, final ChatMessage chatbox){
        //TODO: a member quit from a group
    }

}
