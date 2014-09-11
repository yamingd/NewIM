package com.whosbean.newim.redis;

import com.google.common.collect.Lists;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;
import redis.clients.util.Pool;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by yaming_deng on 14-9-5.
 */
@Component
public class RedisBucket extends Pool<BinaryShardedJedis> implements InitializingBean {

    private RedisConfig jedisConfig;

    private JedisPoolConfig jedisPoolConfig;
    private JedisShardInfo jedisShardInfo;

    public RedisBucket(){
        this.jedisConfig = new RedisConfig();
        try {
            this.jedisConfig.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxActive(jedisConfig.get(Integer.class,"maxActive", 50));
        jedisPoolConfig.setMaxIdle(jedisConfig.get(Integer.class,"maxIdle", 10));
        jedisPoolConfig.setMaxWait(jedisConfig.get(Integer.class,"maxWait", 5000));

        String host = jedisConfig.get(String.class, "host");
        int port = jedisConfig.get(Integer.class, "port", 6379);
        jedisShardInfo = new JedisShardInfo(host, port);

        List<JedisShardInfo> shards = Lists.newArrayList();
        shards.add(jedisShardInfo);

        this.initPool(jedisPoolConfig, new ShardedJedisFactory(shards, Hashing.MURMUR_HASH, null));
    }

    /**
     * PoolableObjectFactory custom impl.
     */
    private static class ShardedJedisFactory extends BasePoolableObjectFactory {
        private List<JedisShardInfo> shards;
        private Hashing algo;
        private Pattern keyTagPattern;

        public ShardedJedisFactory(List<JedisShardInfo> shards, Hashing algo,
                                   Pattern keyTagPattern) {
            this.shards = shards;
            this.algo = algo;
            this.keyTagPattern = keyTagPattern;
        }

        public Object makeObject() throws Exception {
            BinaryShardedJedis jedis = new BinaryShardedJedis(shards, algo, keyTagPattern);
            return jedis;
        }

        public void destroyObject(final Object obj) throws Exception {
            if ((obj != null) && (obj instanceof ShardedJedis)) {
                ShardedJedis shardedJedis = (ShardedJedis) obj;
                for (Jedis jedis : shardedJedis.getAllShards()) {
                    try {
                        try {
                            jedis.quit();
                        } catch (Exception e) {

                        }
                        jedis.disconnect();
                    } catch (Exception e) {

                    }
                }
            }
        }

        public boolean validateObject(final Object obj) {
            try {
                ShardedJedis jedis = (ShardedJedis) obj;
                for (Jedis shard : jedis.getAllShards()) {
                    if (!shard.ping().equals("PONG")) {
                        return false;
                    }
                }
                return true;
            } catch (Exception ex) {
                return false;
            }
        }
    }

}
