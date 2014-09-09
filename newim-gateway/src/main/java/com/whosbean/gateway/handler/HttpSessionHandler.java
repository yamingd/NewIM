package com.whosbean.gateway.handler;

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
        } else {
            System.out.println("No Cookie in websocket request");
        }

        ctx.pipeline().remove(this); // remove after auth'd

        ctx.fireChannelRead(req);
    }
}
