package com.chl.blogapi.cental.threadpool;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.chl.blogapi.cental.service.ViewUserService;
import com.chl.blogapi.util.ThreadPoolUtil;

/**
 * @Auther: BoYanG
 * @Describe 异步操作
 */
public class SynaOperationUtil {

    private static final Log logger = LogFactory.getLog(SynaOperationUtil.class);

    private static final String USERNAME_COLLECTIONS = "Collection:username";
    private static final String USERMAIL_COLLECTIONS = "Collection:usermail";

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

    // 更新用户名缓存
    public static void updateUserNames(final StringRedisTemplate redisTemplate,final ViewUserService viewUserService,final String username) {
        Runnable updateNames = () -> {
            if (username != null) {
                cleanUserCache(redisTemplate, username);
            }
            redisTemplate.delete(USERNAME_COLLECTIONS);
            redisTemplate.opsForValue().set(USERNAME_COLLECTIONS, ","+viewUserService.findAllUserNames()+",", 2, TimeUnit.DAYS);
        };
        ThreadPoolUtil.execute(updateNames);
    }

    public static void cleanUserCache(StringRedisTemplate redisTemplate, String username){
        redisTemplate.delete("UserInfo:"+username+"_cache");
    }


    //更新邮箱集合缓存
    public static void updateUserMails(StringRedisTemplate redisTemplate, ViewUserService viewUserService) {
        Runnable usermails = () -> {
            redisTemplate.delete(USERMAIL_COLLECTIONS);
            redisTemplate.opsForValue().set(USERMAIL_COLLECTIONS, ","+viewUserService.findAllUserMails()+",", 2, TimeUnit.DAYS);
        };
        ThreadPoolUtil.execute(usermails);
    }
}
