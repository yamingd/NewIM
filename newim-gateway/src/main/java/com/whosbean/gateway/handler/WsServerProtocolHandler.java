package com.whosbean.gateway.handler;

import com.whosbean.gateway.connection.ChannelsHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by yaming_deng on 14-9-9.
 */
public class WsServerProtocolHandler extends WebSocketServerProtocolHandler {

    protected static Logger logger = LoggerFactory.getLogger(WsServerProtocolHandler.class);

    public WsServerProtocolHandler(String websocketPath) {
        super(websocketPath);
    }

    public WsServerProtocolHandler(String websocketPath, String subprotocols) {
        super(websocketPath, subprotocols);
    }

    public WsServerProtocolHandler(String websocketPath, String subprotocols, boolean allowExtensions) {
        super(websocketPath, subprotocols, allowExtensions);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) throws Exception {
        if (frame instanceof CloseWebSocketFrame) {
            ChannelsHolder.remove(ctx.channel());
            super.decode(ctx, frame, out);
            return;
        }
        super.decode(ctx, frame, out);
    }

}
