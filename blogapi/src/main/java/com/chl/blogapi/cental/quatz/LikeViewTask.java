package com.chl.blogapi.cental.quatz;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.chl.blogapi.cental.service.ArticleService;
import com.chl.blogapi.cental.service.UserLikeService;
import com.chl.blogapi.cental.threadpool.SynaOperationUtil;
import com.chl.blogapi.util.DateUtil;
import com.chl.blogapi.util.RedisLikeUtil;

/**
 * @Auther: BoYanG
 * @Describe 定时器点赞以及阅读量从redis同步到数据库
 */
public class LikeViewTask extends QuartzJobBean {

    private static Log logger = LogFactory.getLog(LikeViewTask.class);

    @Autowired
    UserLikeService likedService;

    @Autowired
    ArticleService articleService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        logger.info(String.format("LikeTask-------- %s", DateUtil.now()));

        //将 Redis 里的点赞信息同步到数据库里
        //文章点赞同步
        likedService.transArticleLikedFromRedis2DB(RedisLikeUtil.MAP_KEY_USER_LIKED);
        likedService.transArticleLikedCountFromRedis2DB(RedisLikeUtil.MAP_KEY_USER_LIKED_COUNT);

        //评论点赞同步
        likedService.transCommentLikedFromRedis2DB(RedisLikeUtil.COMMENT_KEY_USER_LIKED);
        likedService.transCommentLikedCountFromRedis2DB(RedisLikeUtil.COMMENT_KEY_USER_LIKED_COUNT);

        // 将 Redis中的浏览量同步到数据中
        articleService.transViewCountFromRedisDB();

        SynaOperationUtil.deleteCacheByArticleId(null, stringRedisTemplate, true);

    }

}
