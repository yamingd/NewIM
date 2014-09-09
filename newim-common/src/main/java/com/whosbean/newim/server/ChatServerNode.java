package com.whosbean.newim.server;

import com.whosbean.newim.entity.ChatMessage;
import com.whosbean.newim.entity.Chatbox;
import io.netty.channel.Channel;

/**
 * Created by yaming_deng on 14-9-9.
 */
public class ChatServerNode extends ServerNode {

    public void addConnection(final Channel channel, final Chatbox chatbox){

    }

    public void newMessage(final Channel channel, final ChatMessage chatMessage){

    }

    public void removeConnection(final Channel channel){

    }

    public void joinGroup(final Channel channel, final Chatbox chatbox){

    }

    public void quitGroup(final Channel channel, final Chatbox chatbox){

    }

    public void quit(final Channel channel, final Chatbox chatbox){

    }

}
