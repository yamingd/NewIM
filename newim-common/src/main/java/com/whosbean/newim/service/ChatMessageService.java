package com.whosbean.newim.service;

import com.whosbean.newim.entity.ChatMessage;

/**
 * Created by yaming_deng on 14-9-11.
 */
public interface ChatMessageService {

    /**
     *
     * @param message
     */
    void save(ChatMessage message);

    /**
     *
     * @param uuid
     * @return
     */
    ChatMessage get(String uuid);

    /**
     * 读取字节
     * @param uuid
     * @return
     */
    byte[] getBytes(String uuid);
    /**
     *
     * @param uuid
     */
    boolean remove(String uuid);
}
