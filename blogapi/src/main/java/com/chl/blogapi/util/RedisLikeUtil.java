package com.chl.blogapi.util;

import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.chl.blogapi.cental.domain.Article;
import com.chl.blogapi.cental.domain.UserArticleLike;
import com.chl.blogapi.cental.repository.UserRepository;
import com.chl.blogapi.cental.service.RedisLikeService;
import com.chl.blogapi.cental.service.UserLikeService;

/**
 * @Auther: BoYanG
 * redis操作
 */
public class RedisLikeUtil {

    //保存文章点赞数据的key
    public static final String MAP_KEY_USER_LIKED = "MAP_USER_LIKED";
    //保存文章被点赞数量的key
    public static final String MAP_KEY_USER_LIKED_COUNT = "MAP_USER_LIKED_COUNT";

    //评论点赞数据的key
    public static final String COMMENT_KEY_USER_LIKED = "COMMENT_USER_LIKED";
    //评论点赞数量的key
    public static final String COMMENT_KEY_USER_LIKED_COUNT = "COMMENT_USER_LIKED_COUNT";

    //文章浏览量，点击进入一次则文章浏览量+1
    public static final String ARTICLE_VIEW_TIMES = "ARTICLE_VIEW_TIMES:";


    //发送邮件相关redis key
    public static String mailKey(String mail){
        return "mailRegister:mail="+mail;
    }

    public static String keyLimit60(String mailKey){
        return mailKey+"limit60";
    }

    public static String randomCheckcode(){
        return RandomStringUtils.randomNumeric(6);
    }


    public static String getArticleKey(Integer id){
        return ARTICLE_VIEW_TIMES+String.valueOf(id);
    }

    /**
     * 拼接被点赞的文章id和点赞的人的id作为key。格式 222222::333333
     * @param articleId 被点赞的文章ID
     * @param ip 点赞的人的IP地址
     * @return
     */
    public static String getLikedKey(Integer articleId, String ip){
        StringBuilder builder = new StringBuilder();
        builder.append(String.valueOf(articleId));
        builder.append("::");
        builder.append(ip);
        return builder.toString();
    }

    /**
     * 点赞或者取消赞
     * @param redisLikeService
     * @param status
     * @param aid
     */
    public static void likeOrUnLike(RedisLikeService redisLikeService, Integer status, Integer aid, String username, String mapKey, String mapCountKey){
        if (status==1) {
            redisLikeService.saveLiked2Redis(aid, username, mapKey);
            redisLikeService.incrementLikedCount(aid, mapCountKey);
            return;
        }
        redisLikeService.unlikeFromRedis(aid, username, mapKey);
        redisLikeService.decrementLikedCount(aid, mapCountKey);
    }


    /**
     * 更新列表中的阅读量、点赞数、点赞状态---不可缓存
     * @param stringRedisTemplate
     * @param articles
     * @return
     */
    public static List<Article> updateCacheCount(StringRedisTemplate stringRedisTemplate, List<Article> articles, String username,
                                                 RedisLikeService redisLikeService, UserLikeService userLikeService){
        for (Article article : articles) {

            // 更新阅读量
            String count = stringRedisTemplate.opsForValue().get(RedisLikeUtil.getArticleKey(article.getArticleId()));
            if (count!=null) {
                article.setArticleViews(article.getArticleViews()+Integer.parseInt(count));
            }

            if (StringUtils.isNotEmpty(username)) {
                //点赞数
                Integer aid = article.getArticleId();
                Object redisVal = redisLikeService.getObjectFromRedis(RedisLikeUtil.getLikedKey(aid, username),RedisLikeUtil.MAP_KEY_USER_LIKED);
                UserArticleLike u = userLikeService.getUserLikeByIpAndAid(username, aid);

                if (redisVal != null) {
                    article.setIsLike((Integer)redisVal);
                    Integer likeCount = article.getArticleLikeCount();
                    if (u!=null && !u.getStatus().equals(redisVal)) {
                        article.setArticleLikeCount((Integer)redisVal==1?likeCount+1:(likeCount-1<0?0:likeCount-1));
                    }
                    if (u==null && (Integer)redisVal==1) {
                        article.setArticleLikeCount(likeCount+1);
                    }

                } else {
                    //点赞状态
                    if (u!=null)
                        article.setIsLike(u.getStatus());
                }
            }
        }
        return articles;
    }

    /**
     * 更新列表中的公共信息，左侧图片，标签列表以及作者信息，可缓存
     * @param articles
     * @param userRepository
     * @return
     */
    public static List<Article> updateArticlePubInfo(List<Article> articles, UserRepository userRepository){
        for (Article art : articles) {
            if (userRepository!=null) {
                art.setBase64ImgByContent(art.getArticleContent());
                art.setAuthor(userRepository.findById(art.getUserId()).get().getLoginAccount());
            } else {
                art.setLabelNamesByNames(art.getLabelName());
            }
        }
        return articles;
    }

}
