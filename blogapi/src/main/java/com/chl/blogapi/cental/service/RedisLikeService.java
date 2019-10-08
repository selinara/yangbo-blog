package com.chl.blogapi.cental.service;

import java.util.List;

import com.chl.blogapi.cental.domain.LikedCountDTO;
import com.chl.blogapi.cental.domain.UserArticleLike;

/**
 * @Auther: BoYanG
 * @Describe TODO
 */
public interface RedisLikeService {
    /**
     * 点赞。状态为1
     */
    void saveLiked2Redis(Integer articleId, String username, String redisKey);

    /**
     * 取消点赞。将状态改变为0
     */
    void unlikeFromRedis(Integer articleId, String username, String redisKey);

    /**
     * 从Redis中删除一条点赞数据
     */
    void deleteLikedFromRedis(Integer articleId, String username, String redisKey);

    /**
     * 该用户的点赞数加1
     */
    void incrementLikedCount(Integer articleId, String redisKey);

    /**
     * 该用户的点赞数减1
     */
    void decrementLikedCount(Integer articleId, String redisKey);

    /**
     * 获取Redis中存储的所有点赞数据
     * @return
     */
    List<UserArticleLike> getLikedDataFromRedis(String redisKey);

    /**
     * 获取Redis中存储的所有点赞数量
     * @return
     */
    List<LikedCountDTO> getLikedCountFromRedis(String redisKey);

    Object getObjectFromRedis(String key, String redisKey);
}
