package com.chl.blogapi.util;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

/**
 * @Auther: BoYanG
 * @Describe 接口防撞
 */
public class SimpleRateLimiter {

    private static final Log logger = LogFactory.getLog(SimpleRateLimiter.class);

    private final String limiterName;
    private final String blockedFlag;
    private volatile int limit;
    private final int window;
    private final int block;
    private final String limiterConfig;

    /**
     *  initialize SimpleRateLimiter with fixed rate
     *  or set limiter later by invoking setLimit()
     * @param identifier
     * @param limit
     * @param window time window in sec
     * @param block
     */
    public SimpleRateLimiter(String identifier, int limit, int window, int block) {
        Assert.isTrue(window > 0, "window should be greater than 0");
        Assert.isTrue(block >= 0, "block should be equal or greater than 0");
        this.limit = limit;
        this.window = window;
        this.block = block;
        this.limiterName = "__rate_limiter:" + identifier + ":";
        this.blockedFlag = "__blocked:" + identifier + ":";
        this.limiterConfig = null;
    }

    /**
     * initialize SimpleRateLimiter with configurable rate
     * @param identifier
     * @param limiterConfig config entry read from backend
     * @param window 单位时间内的访问限制
     * @param block 超过访问限制锁定时间
     */
    public SimpleRateLimiter(String identifier, String limiterConfig, int window, int block) {
        this.window = window;
        this.block = block;
        this.limiterConfig = limiterConfig;
        this.limiterName = "__rate_limiter:" + identifier + ":";
        this.blockedFlag = "__blocked:" + identifier + ":";
    }

    public static boolean isWhiteIp(String ip) {
        List<String> ipWhiteList = ConstantUtil.getIpWhiteList();
        for(String whiteIp : ipWhiteList){
            if(ip.equals(whiteIp)) {
                return true;
            }else{
                continue;
            }
        }
        return false;
    }

    public void setLimit(int limit) { this.limit = limit;}

    public String getLimiterKey(String key) {
        return limiterName + key;
    }

    public String getBlockedKey(String key) {
        return blockedFlag + key;
    }

    /**
     * acquire permission for given resource under specified rate limiter
     * @param key
     * @return
     */
    public boolean acquire(String key, StringRedisTemplate redisTemplate) {

        if (block > 0) {
            String releaseTime = redisTemplate.opsForValue().get(getBlockedKey(key));
            if (!StringUtils.isEmpty(releaseTime)) {
                logger.info("key:" + getLimiterKey(key) + " blocked, release time:" + releaseTime);
                return false;
            }
        }

        int limit = this.limiterConfig != null ? ConstantUtil.getLimiterByKey(this.limiterConfig) : this.limit;

        if (limit > 0) {
            try {

                final byte[] limiterKey = redisTemplate.getStringSerializer().serialize(getLimiterKey(key));

                List<Object> resp = redisTemplate.execute((RedisCallback<List<Object>>) connection -> {
                    connection.openPipeline();
                    connection.incr(limiterKey);//每次访问自增1
                    connection.ttl(limiterKey);//单位时间剩余s
                    return connection.closePipeline();
                });

                long cnt = (Long)resp.get(0);
                long ttl = (Long)resp.get(1);

                if (cnt == 1) {
                    redisTemplate.expire(getLimiterKey(key), window, TimeUnit.SECONDS);
                    return true;
                } else if (ttl == -1 ) { // dead lock proof
                    redisTemplate.expire(getLimiterKey(key), window, TimeUnit.SECONDS);
                }

                if (cnt <= limit) {
                    return true;
                } else {
                    if (block > 0) {
                        redisTemplate.opsForValue().setIfAbsent(getBlockedKey(key), DateTime.now().plusSeconds(block).toString("yyyy-MM-dd HH:mm:ss"), block, TimeUnit.SECONDS);
                    }
                    logger.info("key:" + getLimiterKey(key) + " limited, cnt: " + cnt + " limit: " + limit);
                    return false;
                }
            } catch (Exception e) {
                logger.error("limiter failed", e);
                return true;
            } finally {

            }

        } else {
            logger.warn("the limit set to " + limit +", limiter skipped for " + getLimiterKey(key));
            return true;
        }
    }

    /**
     * reset the limiter
     * @param key
     */
    public void reset(String key, StringRedisTemplate redisTemplate) {
        redisTemplate.delete(Arrays.asList(new String[]{getBlockedKey(key), getLimiterKey(key)}));
    }

}
