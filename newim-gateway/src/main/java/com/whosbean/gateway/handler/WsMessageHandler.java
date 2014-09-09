package com.whosbean.gateway.handler;

import com.whosbean.gateway.connection.ChannelsHolder;
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

public class WsMessageHandler extends SimpleChannelInboundHandler<WebSocketFrame>
{

    protected static Logger logger = LoggerFactory.getLogger(WsMessageHandler.class);

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

            //TODO: send message to specific client or a group.

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
            int len = b.content().readableBytes();
            byte[] bytes = new byte[len];
            b.content().readBytes(bytes);
        }
    }

    protected void handleMessage(){

    }
}
