package com.chl.gbo.cental.threadpool;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.chl.gbo.cental.service.ViewUserService;
import com.chl.gbo.util.ThreadPoolUtil;

/**
 * @Auther: BoYanG
 * @Describe 异步操作
 */
public class SynaOperationUtil {

    private static final Log logger = LogFactory.getLog(SynaOperationUtil.class);

    //异步清除redis缓存
    public static void deleteCacheByArticleId(Integer articleId, StringRedisTemplate redisTemplate,Boolean clearAll){
        DeleteCacheRunnable deleteCacheRunnable = new DeleteCacheRunnable(articleId, redisTemplate, clearAll);
        ThreadPoolUtil.execute(deleteCacheRunnable);
    }

    // 清除友情链接缓存
    public static void cleanFriendShipCache(final StringRedisTemplate redisTemplate) {
        Runnable frenshipRunable = () -> {redisTemplate.delete("friendShipLinks:cache");logger.info("清除友情链接缓存成功");};
        ThreadPoolUtil.execute(frenshipRunable);
    }
}
