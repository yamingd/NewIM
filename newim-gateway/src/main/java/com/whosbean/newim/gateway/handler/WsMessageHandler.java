package com.whosbean.newim.gateway.handler;

import com.whosbean.newim.gateway.GatewayConfig;
import com.whosbean.newim.gateway.GatewayServerNode;
import com.whosbean.newim.gateway.connection.ChannelsHolder;
import com.whosbean.newim.gateway.connection.WebSession;
import com.whosbean.newim.common.MessageUtil;
import com.whosbean.newim.entity.ChatMessage;
import com.whosbean.newim.service.ChatMessageService;
import com.whosbean.newim.service.ChatMessageServiceFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class WsMessageHandler extends SimpleChannelInboundHandler<WebSocketFrame>
{

    protected static Logger logger = LoggerFactory.getLogger(WsMessageHandler.class);

    protected ChatMessageService chatMessageService;

    public WsMessageHandler() {
        chatMessageService = ChatMessageServiceFactory.get(GatewayConfig.current);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception
    {
        ctx.close();
    }

    /**
     * We implement this to catch the websocket handshake completing
     * successfully. At that point we'll setup this client connection.
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception
    {
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            configureClient(ctx);
        }
    }

    /**
     * Should end up being called after websocket handshake completes. Will
     * configure this client for communication with the application.
     */
    protected void configureClient(ChannelHandlerContext ctx) {
        ChannelsHolder.add(ctx.channel());
        System.out.println("Checking auth");
    }

    /**
     * When a message is sent into the app by the connected user this is
     * invoked.
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception
    {
        if (frame instanceof TextWebSocketFrame){
            TextWebSocketFrame text = (TextWebSocketFrame)frame;
            System.out.println(text.text());
            ChannelFuture future = ctx.channel().writeAndFlush(new TextWebSocketFrame(text.text()));
            future.addListener(new GenericFutureListener<Future<Void>>() {
                @Override
                public void operationComplete(Future<Void> future) throws Exception {
                    System.out.println("write to channels successful");
                }
            });

        }else if(frame instanceof BinaryWebSocketFrame){
            BinaryWebSocketFrame b = (BinaryWebSocketFrame)frame;
            this.handleMessage(ctx, b.content());
        }
    }

    private WebSession getSession(ChannelHandlerContext ctx){
        WebSession session = ctx.channel().attr(ChannelAttributes.SESSIOON_ATTR_KEY).get();
        return session;
    }

    protected void handleMessage(ChannelHandlerContext ctx, ByteBuf bytes) throws IOException {
        ChatMessage chatMessage = MessageUtil.asT(ChatMessage.class, bytes);
        WebSession session = getSession(ctx);
        chatMessage.sender = session.getUid();
        int value = chatMessage.op.intValue();
        if (value == ChatMessage.OP_JOIN){
            GatewayServerNode.current.join(ctx.channel(), chatMessage);
        }else if (value == ChatMessage.OP_QUIT){
            GatewayServerNode.current.quit(ctx.channel(), chatMessage);
        }else if (value == ChatMessage.OP_CHAT){
            this.chatMessageService.save(chatMessage);
            GatewayServerNode.current.newMessage(ctx.channel(), chatMessage);
        }
    }
}
