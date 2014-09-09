package com.whosbean.newim.common;

import io.netty.buffer.ByteBuf;
import org.msgpack.MessagePack;

import java.io.IOException;

/**
 * Created by yaming_deng on 14-9-9.
 */
public class MessageUtil {

    private static MessagePack messagePack = new MessagePack();

    public static <T> T asT(Class<T> clazz, byte[] bytes) throws IOException {
        return messagePack.read(bytes, clazz);
    }

    public static <T> byte[] asBytes(T o) throws IOException {
        byte[] bytes = messagePack.write(o);
        return bytes;
    }

    public static <T> T asT(Class<T> clazz, ByteBuf msg) throws IOException {
        byte[] dd = new byte[msg.readableBytes()];
        msg.readBytes(dd);
        return asT(clazz, dd);
    }

}
