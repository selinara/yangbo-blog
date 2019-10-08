package com.chl.blogapi.cental.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import com.chl.blogapi.cental.domain.LikedCountDTO;
import com.chl.blogapi.cental.domain.UserArticleLike;
import com.chl.blogapi.cental.enumclass.UserLikeEnum;
import com.chl.blogapi.cental.service.RedisLikeService;
import com.chl.blogapi.util.RedisLikeUtil;

@Service
public class RedisLikeServiceImpl implements RedisLikeService {

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public void saveLiked2Redis(Integer articleId, String username, String redisKey) {
        String key = RedisLikeUtil.getLikedKey(articleId, username);
        redisTemplate.opsForHash().put(redisKey, key, UserLikeEnum.LIKE.getCode());
    }

    @Override
    public void unlikeFromRedis(Integer articleId, String username, String redisKey) {
        String key = RedisLikeUtil.getLikedKey(articleId, username);
        redisTemplate.opsForHash().put(redisKey, key, UserLikeEnum.UNLIKE.getCode());
    }

    @Override
    public void deleteLikedFromRedis(Integer articleId, String username, String redisKey) {
        String key = RedisLikeUtil.getLikedKey(articleId, username);
        redisTemplate.opsForHash().delete(redisKey, key);
    }

    @Override
    public void incrementLikedCount(Integer articleId, String redisKey) {
        redisTemplate.opsForHash().increment(redisKey, articleId, (long) 1);
    }

    @Override
    public void decrementLikedCount(Integer articleId, String redisKey) {
        redisTemplate.opsForHash().increment(redisKey, articleId, (long)-1);
    }

    @Override
    public List<UserArticleLike> getLikedDataFromRedis(String redisKey) {
        Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(redisKey, ScanOptions.NONE);
        List<UserArticleLike> list = new ArrayList<>();
        while (cursor.hasNext()){
            Map.Entry<Object, Object> entry = cursor.next();
            String key = (String) entry.getKey();
            //分离出 articleId，ip
            String[] split = key.split("::");
            Integer articleId = Integer.parseInt(split[0]);
            String ip = split[1];
            Integer value = (Integer) entry.getValue();

            //组装成 UserLike 对象
            UserArticleLike userLike = new UserArticleLike(ip, articleId, value);
            list.add(userLike);

            //存到 list 后从 Redis 中删除
            redisTemplate.opsForHash().delete(redisKey, key);
        }

        return list;
    }

    @Override
    public List<LikedCountDTO> getLikedCountFromRedis(String redisKey) {
        Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(redisKey, ScanOptions.NONE);
        List<LikedCountDTO> list = new ArrayList<>();
        while (cursor.hasNext()){
            Map.Entry<Object, Object> map = cursor.next();
            //将点赞数量存储在 LikedCountDT
            Integer key = (Integer)map.getKey();
            LikedCountDTO dto = new LikedCountDTO(key, (Integer) map.getValue());
            list.add(dto);
            //从Redis中删除这条记录
            redisTemplate.opsForHash().delete(redisKey, key);
        }
        return list;
    }

    @Override
    public Object getObjectFromRedis(String key, String redisKey) {
        return redisTemplate.opsForHash().get(redisKey, key);
    }
}
