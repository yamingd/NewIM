package com.whosbean.newim.service;

import com.whosbean.newim.common.AppContextHolder;
import com.whosbean.newim.config.AbstractConfig;

import java.util.Map;

/**
 * Created by yaming_deng on 14-9-11.
 */
public class ChatMessageServiceFactory {

    public static final String MESSAGE_SERVICE = "MessageService";

    public static ChatMessageService get(String name){
        String beanName = name + MESSAGE_SERVICE;
        ChatMessageService service = AppContextHolder.context.get(ChatMessageService.class, beanName);
        return service;
    }

    public static ChatMessageService get(AbstractConfig config){
        Map<String, Object> map = config.get(Map.class, "bucket");
        String beanName = map.get("engine") + MESSAGE_SERVICE;
        ChatMessageService service = AppContextHolder.context.get(ChatMessageService.class, beanName);
        return service;
    }
}
