package com.chl.blogapi.cental.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chl.blogapi.cental.domain.Article;
import com.chl.blogapi.cental.domain.LikedCountDTO;
import com.chl.blogapi.cental.domain.User;
import com.chl.blogapi.cental.domain.UserArticleLike;
import com.chl.blogapi.cental.domain.UserCommentLike;
import com.chl.blogapi.cental.repository.ArticleRepository;
import com.chl.blogapi.cental.repository.UserCommentRepository;
import com.chl.blogapi.cental.repository.UserLikeRepository;

/**
 * db操作
 */
@Service
public class UserLikeService {

    @Autowired
    UserLikeRepository likeRepository;
    @Autowired
    UserCommentRepository userCommentRepository;

    @Autowired
    RedisLikeService redisService;

    @Autowired
    ArticleRepository articleRepository;

    @Transactional
    public UserArticleLike saveUserLike(UserArticleLike userLike) {
        return likeRepository.save(userLike);
    }

    @Transactional
    public UserCommentLike saveCommentLike(UserCommentLike userCommentLike) {
        return userCommentRepository.save(userCommentLike);
    }

    @Transactional
    public List<UserArticleLike> saveAllUserLike(List<UserArticleLike> list) {
        return likeRepository.saveAll(list);
    }

    @Transactional
    public List<UserCommentLike> saveAllUserComment(List<UserCommentLike> list) {
        return userCommentRepository.saveAll(list);
    }


    public UserArticleLike getUserLikeByIpAndAid(String ip, Integer aid) {
        return likeRepository.getUserLikeByIpAndAid(ip, aid);
    }

    public UserCommentLike getCommentLikeByUserNameAndAid(String username, Integer aid) {
        return userCommentRepository.getCommentLikeByUserNameAndAid(username, aid);
    }

    @Transactional
    public void transArticleLikedFromRedis2DB(String redisKey) {
        List<UserArticleLike> list = redisService.getLikedDataFromRedis(redisKey);
        for (UserArticleLike like : list) {
            UserArticleLike ul = getUserLikeByIpAndAid(like.getIp(), like.getArticleId());
            if (ul == null){
                //没有记录，直接存入
                saveUserLike(like);
            }else{
                //有记录，需要更新
                ul.setStatus(like.getStatus());
                saveUserLike(ul);
            }
        }
    }

    @Transactional
    public void transCommentLikedFromRedis2DB(String redisKey) {
        List<UserArticleLike> list = redisService.getLikedDataFromRedis(redisKey);
        for (UserArticleLike like : list) {
            UserCommentLike ul = getCommentLikeByUserNameAndAid(like.getIp(), like.getArticleId());
            if (ul == null){
                //没有记录，直接存入
                UserCommentLike userCommentLike = new UserCommentLike(like.getIp(), like.getArticleId(), like.getStatus());
                saveCommentLike(userCommentLike);
            }else{
                //有记录，需要更新
                ul.setStatus(like.getStatus());
                saveCommentLike(ul);
            }
        }
    }

    @Transactional
    public void transArticleLikedCountFromRedis2DB(String redisKey) {
        List<LikedCountDTO> list = redisService.getLikedCountFromRedis(redisKey);
        for (LikedCountDTO dto : list) {
            Optional<Article> opt = articleRepository.findById(dto.getArticleId());
            if (opt == null) {
                continue;
            }
            Article article = opt.get();
            //点赞数量属于无关紧要的操作，出错无需抛异常
            if (article != null){
                Integer likeNum = article.getArticleLikeCount() + dto.getCount();
                article.setArticleLikeCount(likeNum);
                //更新点赞数量
                articleRepository.save(article);
            }
        }
    }
    @Transactional
    public void transCommentLikedCountFromRedis2DB(String redisKey) {
        List<LikedCountDTO> list = redisService.getLikedCountFromRedis(redisKey);
        for (LikedCountDTO dto : list) {

            List<Map> comments = articleRepository.getCommentsById(dto.getArticleId());

            if(comments.size() > 0){
                Map map = comments.get(0);
                Integer newNum = Integer.parseInt(map.get("commentLikeCount").toString()) + dto.getCount();
                articleRepository.updateLikeCountById(newNum, dto.getArticleId());
            }
        }
    }

}
