package com.whosbean.newim.redis;

import com.whosbean.newim.common.MessageUtil;
import com.whosbean.newim.entity.ChatMessage;
import com.whosbean.newim.service.ChatMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.BinaryShardedJedis;

/**
 * Created by yaming_deng on 14-9-11.
 */
@Service
public class RedisMessageService implements ChatMessageService {

    public static final String IM_PENDING = "im:qs:";
    public static final byte[] IM_PENDING_COUNT = "im:qsc".getBytes();
    public static final byte[] IM_PK = "im:pk".getBytes();

    public static final int TTS = 600;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisBucket redisBucket;

    @Override
    public void save(ChatMessage message) {
        BinaryShardedJedis jedis =  redisBucket.getResource();
        try {
            long id = jedis.incr(IM_PK);
            message.uuid = id + "";
            byte[] key = (IM_PENDING + id).getBytes();
            jedis.set(key, MessageUtil.asBytes(message));
            jedis.expire(key, TTS);
            long total = jedis.incr(IM_PENDING_COUNT);
            redisBucket.returnResource(jedis);
            logger.info("pending total = " + total);
        } catch (Exception e) {
            logger.error("添加消息进Redis错误", e);
            redisBucket.returnBrokenResource(jedis);
        }
    }

    @Override
    public ChatMessage get(String uuid) {
        BinaryShardedJedis jedis =  redisBucket.getResource();
        try {
            byte[] key = (IM_PENDING + uuid).getBytes();
            byte[] data = jedis.get(key);
            ChatMessage message = MessageUtil.asT(ChatMessage.class, data);
            redisBucket.returnResource(jedis);
            return message;
        } catch (Exception e) {
            logger.error("添加消息进Redis错误", e);
            redisBucket.returnBrokenResource(jedis);
        }
        return null;
    }

    @Override
    public byte[] getBytes(String uuid) {
        BinaryShardedJedis jedis =  redisBucket.getResource();
        try {
            byte[] key = (IM_PENDING + uuid).getBytes();
            byte[] data = jedis.get(key);
            redisBucket.returnResource(jedis);
            return data;
        } catch (Exception e) {
            logger.error("添加消息进Redis错误", e);
            redisBucket.returnBrokenResource(jedis);
        }
        return null;
    }

    @Override
    public boolean remove(String uuid) {
        BinaryShardedJedis jedis =  redisBucket.getResource();
        try {
            byte[] key = (IM_PENDING + uuid).getBytes();
            long ret = jedis.del(key);
            redisBucket.returnResource(jedis);
            return ret > 0;
        } catch (Exception e) {
            logger.error("添加消息进Redis错误", e);
            redisBucket.returnBrokenResource(jedis);
        }
        return false;
    }
}
