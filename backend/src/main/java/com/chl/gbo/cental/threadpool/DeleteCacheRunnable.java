package com.chl.gbo.cental.threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.thymeleaf.util.StringUtils;
import com.chl.gbo.cental.service.ArticleService;
import lombok.Data;
/**
 * @Auther: BoYanG
 * @Describe 异步清除文章相关缓存
 */
@Data
public class DeleteCacheRunnable implements Runnable {

    private static final Log log = LogFactory.getLog(DeleteCacheRunnable.class);

    private Integer articleId;

    private StringRedisTemplate redisTemplate;

    private Boolean clearAll;

    public DeleteCacheRunnable(Integer articleId, StringRedisTemplate redisTemplate, Boolean clearAll) {
        this.articleId = articleId;
        this.redisTemplate = redisTemplate;
        this.clearAll = clearAll;
    }

    @Override
    public void run() {

        List<String> keyList = new ArrayList<>();

        // 分类页
        Set<String> sortKeys = redisTemplate.keys("articleDetailBySortIdAndPageId*");
        // 标签页
        Set<String> labelKeys = redisTemplate.keys("articleDetailByTagAndPageId*");

        // 主页文章列表缓存
        String homeKey = "homePageArticleList:"+ArticleService.HOME_NEW_RELEASE_ARTICLE_COUNT +"_cache";

        // 置顶文章
        String topKey = "homePageTopArticle:cache";

        // 边列表
        String sideKey = "siteRelease:cache";

        String hotTags = "hotTags:cache";

        StringBuilder asb = new StringBuilder();

        if (articleId!=null) {
            keyList.add("articleById:"+articleId+"_cache");
            asb.append("单篇文章ID为"+articleId+"缓存");
        } else {
            redisTemplate.delete(redisTemplate.keys("articleById*"));
            asb.append("单篇文章缓存集合");
        }

        if (clearAll) {
            keyList.add(homeKey);
            keyList.add(topKey);
            keyList.add(sideKey);
            redisTemplate.delete(sortKeys);
            redisTemplate.delete(labelKeys);
            redisTemplate.delete(hotTags);
            log.info(asb.toString()+"以及其他所有页面缓存清除成功！！");
        } else {
            for (String key : sortKeys) {
                if (redisTemplate.opsForValue().get(key).contains("\"articleId\":"+articleId)) {
                    asb.append("、分类页缓存");
                    keyList.add(key);
                    break;
                }
            }
            for (String key : labelKeys) {
                if (redisTemplate.opsForValue().get(key).contains("\"articleId\":"+articleId)) {
                    asb.append("、标签页缓存");
                    keyList.add(key);
                    break;
                }
            }

            String value = redisTemplate.opsForValue().get(homeKey);
            if(!StringUtils.isEmpty(value) && value.contains("\"articleId\":"+articleId)){
                asb.append("、主页列表缓存");
                keyList.add(homeKey);
            }

            String topValue = redisTemplate.opsForValue().get(topKey);
            if(!StringUtils.isEmpty(topValue) && topValue.contains("\"articleId\":"+articleId)){
                asb.append("、置顶文章缓存");
                keyList.add(topKey);
            }

            String sideValue = redisTemplate.opsForValue().get(sideKey);
            if(!StringUtils.isEmpty(sideValue) && sideValue.contains("\"articleId\":"+articleId)){
                asb.append("、边侧文章缓存");
                keyList.add(sideKey);
            }
            asb.append("清除成功！！");
            log.info(asb.toString());
        }

        redisTemplate.delete(keyList);
    }
}
