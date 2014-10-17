package com.whosbean.newim.gateway.handler;

import com.whosbean.newim.gateway.connection.WebSession;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;


/**
 * Will attempt to find the requestor's session cookie. If the cookie is not
 * found an error message will be returned with a HTTP 400 response code.
 */
public class HttpSessionHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    public HttpSessionHandler() {
        super(false);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req)
            throws Exception {
        if (req.headers().contains("Cookie")) {
            String cookie = req.headers().get("Cookie");
            System.out.println("Got Cookie: " + cookie);
            String uid = cookie;
            ctx.channel().attr(ChannelAttributes.SESSIOON_ATTR_KEY).set(new WebSession(uid));
        } else {
            System.out.println("No Cookie in websocket request");
        }

        ctx.pipeline().remove(this); // remConnection after auth'd
        ctx.fireChannelRead(req);
    }
}
