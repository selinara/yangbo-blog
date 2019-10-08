package com.chl.gbo.util.redislock;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Created by yuduy on 2017/9/27.
 */
@Component
@Service
public class RedisDistributeLock implements DistributedLock {

    private static final Log log = LogFactory.getLog(RedisDistributeLock.class);

    private static final ThreadLocal<Boolean> REDIS_LOCK_HOLD_BY_ME = new ThreadLocal<Boolean>();

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean tryLock(final String key, final int seconds) {
        return redisTemplate.opsForValue().setIfAbsent(key, key, seconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean tryLock(String key, String value, int seconds) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, seconds, TimeUnit.SECONDS);
    }

    @Override
    public void lock(String key, int seconds) {
        while (!tryLock(key, seconds)) {
            //do nothing
        }
    }

    /**
     * unLock with no reason
     * @param key
     * @return
     */
    @Override
    public boolean unLock(String key) {
        return redisTemplate.delete(key);
    }

//    /**
//     * better than tryLock
//     * @param key
//     * @param seconds
//     * @return
//     */
//    @Override
//    public boolean betterTryLock(String key, String value, int seconds) {
//        //get jedis from pool
//        Jedis jedis = RedisLikeUtil.getJedisClient();
//        if (jedis == null) {
//            throw new RedisDistributeLockException(RedisDistributeLockException.CAN_NOT_GET_JEDIS_FROM_POOL, "can't get jedis from pool, key:" + key);
//        }
//
//        try {
//            //set nx
//            //Integer reply, specifically: 1 if the key was set 0 if the key was not set
//            int reply;
//            try {
//                reply = jedis.setnx(key, value).intValue();
//            } catch (Exception e) {//JedisException
//                throw new RedisDistributeLockException(RedisDistributeLockException.CAN_NOT_SET_NX, "can't set nx, key:" + key, e);
//            }
//
//            //expire time
//            if (reply == 1) {
//                REDIS_LOCK_HOLD_BY_ME.set(Boolean.TRUE);
//                try {
//                    jedis.expire(key, seconds);
//                    return true;
//                } catch (Exception e) {
//                    throw new RedisDistributeLockException(RedisDistributeLockException.CAN_NOT_EXPIRE, "can't expire, key:" + key, e);
//                }
//
//            }
//            return false;
//        } finally {
//            RedisLikeUtil.returnResource(jedis);
//        }
//
//    }

    @Override
    public void betterUnLock(String key) {
        if (REDIS_LOCK_HOLD_BY_ME.get() == Boolean.TRUE) {
            REDIS_LOCK_HOLD_BY_ME.remove();
            redisTemplate.delete(key);
        }
    }

    @Override
    public Locker locker(final String key, final int seconds) {
        return new Locker() {

            @Override
            public boolean tryLock() {
                return redisTemplate.opsForValue().setIfAbsent(key, key, seconds, TimeUnit.SECONDS);
            }

            //不建议使用lock方法，后期可能会优化
            @Override
            public void lock() {
                while (!tryLock()) {
                    //do nothing
                }
            }

            //不要再unlock后 调用locked
            @Override
            public boolean unLock() {
                return redisTemplate.delete(key);
            }
        };
    }

}
