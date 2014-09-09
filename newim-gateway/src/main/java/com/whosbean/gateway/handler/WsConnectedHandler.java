package com.whosbean.gateway.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yaming_deng on 14-9-5.
 */
public class WsConnectedHandler extends SimpleChannelInboundHandler<FullHttpRequest>
{
    protected static Logger logger = LoggerFactory.getLogger(WsConnectedHandler.class);

    public WsConnectedHandler() {
        super(false);
    }

    private static final ByteBuf NOT_FOUND =
            Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("NOT FOUND", CharsetUtil.US_ASCII));

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req)
            throws Exception
    {
        String uri = req.getUri();
        // add websocket handler for the request uri where app lives
        ctx.pipeline().addLast(new WsServerProtocolHandler(uri));

        // now add our application handler
        ctx.pipeline().addLast(new WsMessageHandler());

        // remove, app is attached and websocket handler in place
        ctx.pipeline().remove(this);

        // pass the request on
        ctx.fireChannelRead(req);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }
}
