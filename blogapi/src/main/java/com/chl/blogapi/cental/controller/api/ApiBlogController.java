package com.chl.blogapi.cental.controller.api;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.chl.blogapi.cental.domain.Article;
import com.chl.blogapi.cental.enumclass.BaseError;
import com.chl.blogapi.cental.exception.BlogException;
import com.chl.blogapi.cental.service.ArticleService;
import com.chl.blogapi.cental.service.RedisLikeService;
import com.chl.blogapi.cental.service.UserLikeService;
import com.chl.blogapi.cental.service.UserMessageService;
import com.chl.blogapi.cental.threadpool.DeleteCacheRunnable;
import com.chl.blogapi.cental.vo.ArticleDetailPageVO;
import com.chl.blogapi.cental.vo.CommentVO;
import com.chl.blogapi.cental.vo.CommonResultVO;
import com.chl.blogapi.cental.vo.HomePageVO;
import com.chl.blogapi.cental.vo.SortDetailVO;
import com.chl.blogapi.util.HttpUtil;
import com.chl.blogapi.util.RedisLikeUtil;
import com.chl.blogapi.util.SimpleRateLimiter;
import com.chl.blogapi.util.token.TokenUtil;

/**
 * @Auther: BoYanG
 * @Describe 前端api请求
 */
@RestController
@RequestMapping(value = "/api")
public class ApiBlogController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private RedisLikeService redisLikeService;

    @Autowired
    private UserLikeService userLikeService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserMessageService userMessageService;

    //评论
    private SimpleRateLimiter commentAddIpLimiter = new SimpleRateLimiter("comment_ip_limit_60", "ip_limit_60",60, 3600 * 3);
    //留言
    private SimpleRateLimiter messageAddIpLimiter = new SimpleRateLimiter("message_ip_limit_60", "ip_limit_60",60, 3600 * 3);
    //点赞
    private SimpleRateLimiter likeLimiter = new SimpleRateLimiter("like_limiter_ip", "ip_limit_150",60, 3600 * 3);
    //页面刷新接口
    private SimpleRateLimiter pageVisitLimiter = new SimpleRateLimiter("page_visit_ip", "ip_limit_200",60, 3600 * 3);

    /**
     * 首页加载
     * @param cb
     * @return
     */
    @RequestMapping(value = "/home/content/detail", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    public String contentdetail(@RequestParam("callback") String cb,
                                @RequestParam(value = "username" ,required = false) String username,
                                HttpServletRequest request){

        CommonResultVO result = new CommonResultVO();

        String ip = HttpUtil.getIpAddr(request);

        //ip防刷
        if (!SimpleRateLimiter.isWhiteIp(ip)) {
            if (!pageVisitLimiter.acquire(ip, stringRedisTemplate)) {
                result.setErrorMessage(BaseError.IP_TOO_FREQUENT);
                return result.toGsonResultString(cb);
            }
        }

        HomePageVO res = new HomePageVO();
        List<Article> articles = articleService.getHomePageArticleList(DeleteCacheRunnable.HOME_NEW_RELEASE_ARTICLE_COUNT);
        res.setRotations(articleService.getRotations());
        res.setHotTags(articleService.hotTag());
        res.setHomeArticle(RedisLikeUtil.updateCacheCount(stringRedisTemplate, articles, username, redisLikeService, userLikeService));
        res.setSiteArticle(articleService.getRealeaseForm());
        res.setTopArticle(articleService.getHomePageTopArticle());
        res.setFriendShipLink(articleService.getFriendShipLinks());
        result.put("data", res);
        return result.toGsonResultString(cb);
    }

    /**
     * 文章详情
     * @return
     */
    @RequestMapping(value = "/article/detail", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    public String articleDetail(
            @RequestParam("id") Integer id,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam("callback") String cb,
            HttpServletRequest request) throws Exception{
        CommonResultVO result = new CommonResultVO();

        String ip = HttpUtil.getIpAddr(request);

        //ip防刷
        if (!SimpleRateLimiter.isWhiteIp(ip)) {
            if (!pageVisitLimiter.acquire(ip, stringRedisTemplate)) {
                result.setErrorMessage(BaseError.IP_TOO_FREQUENT);
                return result.toGsonResultString(cb);
            }
        }
        //文章浏览量+1
        stringRedisTemplate.opsForValue().increment(RedisLikeUtil.getArticleKey(id), 1);
        ArticleDetailPageVO articleDetailPageVO = articleService.getArticleDetailById(id);
        Article article = articleDetailPageVO.getArticle();
        article = RedisLikeUtil.updateCacheCount(stringRedisTemplate, Arrays.asList(article),username, redisLikeService, userLikeService).get(0);
        articleDetailPageVO.setArticle(article);
        articleDetailPageVO.setFriendShipLink(articleService.getFriendShipLinks());
        result.put("data", articleDetailPageVO);
        return result.toGsonResultString(cb);
    }

    /**
     * 文章详情-加载评论列表
     * @return
     */
    @RequestMapping(value = "/article/comment", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    public String articleComment(
            @RequestParam(value = "articleId", required = false) String articleId,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam("callback") String cb,
            HttpServletRequest request) throws Exception{
        CommonResultVO result = new CommonResultVO();

        String ip = HttpUtil.getIpAddr(request);

        //ip防刷
        if (!SimpleRateLimiter.isWhiteIp(ip)) {
            if (!pageVisitLimiter.acquire(ip, stringRedisTemplate)) {
                result.setErrorMessage(BaseError.IP_TOO_FREQUENT);
                return result.toGsonResultString(cb);
            }
        }
        if (articleId==null) {
            throw new BlogException("文章id不能为空");
        }
        List<CommentVO> vo = articleService.getCommentListByArticleId(articleId, username);
        result.put("result", vo);
        return result.toGsonResultString(cb);
    }

    /**
     * 增加评论
     * @return
     */
    @RequestMapping(value = "/article/comment/add", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    public String articleComment(
            @RequestParam(value = "articleId", required = false) Integer articleId,
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "foruserId", required = false) Integer foruserId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "token", required = false) String token,
            @RequestParam(value = "commentId", required = false, defaultValue = "0") Integer commentId,
            HttpServletRequest request,
            @RequestParam("callback") String cb) throws Exception{
        CommonResultVO result = new CommonResultVO();

        String ip = HttpUtil.getIpAddr(request);

        //ip防刷
        if (!SimpleRateLimiter.isWhiteIp(ip)) {
            if (!commentAddIpLimiter.acquire(ip, stringRedisTemplate)) {
                result.setErrorMessage(BaseError.IP_TOO_FREQUENT);
                return result.toGsonResultString(cb);
            }
        }

        if (articleId==null || userId == null || StringUtils.isEmpty(content)
                || StringUtils.isEmpty(username) || StringUtils.isEmpty(token)) {
            result.setErrorMessage("2", "内容不能为空哦");
            return result.toGsonResultString(cb);
        }

        if (!TokenUtil.isTokenValid(username, token)) {
            result.setErrorMessage(BaseError.TOKEN_VOERDUE);
            return result.toGsonResultString(cb);
        }

        articleService.addArticleComment(articleId, userId,foruserId, commentId, content);
        return result.toGsonResultString(cb);
    }


    /**
     * 评论删除
     * @return
     */
    @RequestMapping(value = "/article/comment/del", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    public String articleCommentDel(
            @RequestParam(value = "commentId", required = false) Integer commentId,
            @RequestParam(value = "articleId", required = false) Integer articleId,
            @RequestParam(value = "parentCommentId", required = false) Integer parentCommentId,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "token", required = false) String token,
            HttpServletRequest request,
            @RequestParam("callback") String cb) throws Exception{
        CommonResultVO result = new CommonResultVO();

        String ip = HttpUtil.getIpAddr(request);

        //ip防刷
        if (!SimpleRateLimiter.isWhiteIp(ip)) {
            if (!commentAddIpLimiter.acquire(ip, stringRedisTemplate)) {
                result.setErrorMessage(BaseError.IP_TOO_FREQUENT);
                return result.toGsonResultString(cb);
            }
        }

        if (commentId==null || articleId==null) {
            result.setErrorMessage("1", "参数不能为空哦");
            return result.toGsonResultString(cb);
        }

        if (!TokenUtil.isTokenValid(username, token)) {
            result.setErrorMessage(BaseError.TOKEN_VOERDUE);
            return result.toGsonResultString(cb);
        }

        articleService.delComment(commentId, parentCommentId, articleId);

        return result.toGsonResultString(cb);
    }

    /**
     * 个人中心-评论列表
     * @return
     */
    @RequestMapping(value = "/article/comment/center", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    public String centerList(
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam("callback") String cb,
            HttpServletRequest request) {
        CommonResultVO result = new CommonResultVO();

        String ip = HttpUtil.getIpAddr(request);

        //ip防刷
        if (!SimpleRateLimiter.isWhiteIp(ip)) {
            if (!pageVisitLimiter.acquire(ip, stringRedisTemplate)) {
                result.setErrorMessage(BaseError.IP_TOO_FREQUENT);
                return result.toGsonResultString(cb);
            }
        }
        if(userId == null){
            result.setErrorMessage("1", "ID不能为空");
            return result.toGsonResultString(cb);
        }
        result.put("result", articleService.centerComment(userId));
        return result.toGsonResultString(cb);
    }

    /**
     * 分类
     */
    @RequestMapping(value = "/article/sort", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    public String articleSort(
            @RequestParam(value = "sid", defaultValue = "2") Integer sid,
            @RequestParam(value = "pid", defaultValue = "1") Integer pid,
            @RequestParam(value = "username" ,required = false) String username,
            @RequestParam("callback") String cb,HttpServletRequest request){
        CommonResultVO result = new CommonResultVO();
        String ip = HttpUtil.getIpAddr(request);

        //ip防刷
        if (!SimpleRateLimiter.isWhiteIp(ip)) {
            if (!pageVisitLimiter.acquire(ip, stringRedisTemplate)) {
                result.setErrorMessage(BaseError.IP_TOO_FREQUENT);
                return result.toGsonResultString(cb);
            }
        }

        // 阅读量
        SortDetailVO sortDetailVO = articleService.getSortListPageDetail(sid, pid);
        sortDetailVO.setHomeArticle(RedisLikeUtil.updateCacheCount(stringRedisTemplate,
                sortDetailVO.getHomeArticle(), username, redisLikeService, userLikeService));
        result.put("data", sortDetailVO);
        return result.toGsonResultString(cb);
    }

    /**
     * 标签/检索
     */
    @RequestMapping(value = "/article/list", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    public String tag(
            @RequestParam(value = "id", defaultValue = "0") Integer id,
            @RequestParam(value = "pid", defaultValue = "1") Integer pid,
            @RequestParam(value = "username" ,required = false) String username,
            @RequestParam(value = "search",required = false) String content,
            @RequestParam("callback") String cb,HttpServletRequest request){
        CommonResultVO result = new CommonResultVO();
        SortDetailVO sortDetailVO = new SortDetailVO();
        String ip = HttpUtil.getIpAddr(request);

        //ip防刷
        if (!SimpleRateLimiter.isWhiteIp(ip)) {
            if (!pageVisitLimiter.acquire(ip, stringRedisTemplate)) {
                result.setErrorMessage(BaseError.IP_TOO_FREQUENT);
                return result.toGsonResultString(cb);
            }
        }
        // 阅读量
        if (id != 0 && id != null) {
            sortDetailVO = articleService.getTagListPageDetail(id, pid);
        } else if (StringUtils.isNotEmpty(content)) {
            sortDetailVO = articleService.getContentListPageDetail(content, pid);
        }
        sortDetailVO.setHomeArticle(RedisLikeUtil.updateCacheCount(stringRedisTemplate,
                sortDetailVO.getHomeArticle(),username, redisLikeService, userLikeService));
        result.put("data", sortDetailVO);
        return result.toGsonResultString(cb);

    }

    /**
     * 文章（点赞/取消赞）
     */
    @RequestMapping(value = "/article/like", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    public String articleLike(
            @RequestParam(value = "aid") Integer aid,
            @RequestParam(value = "username") String username,
            @RequestParam(value = "token") String token,
            @RequestParam(value = "status", defaultValue = "0") Integer status,
            @RequestParam("callback") String cb,HttpServletRequest request){
        CommonResultVO result = new CommonResultVO();

        String ip = HttpUtil.getIpAddr(request);

        //ip防刷
        if (!SimpleRateLimiter.isWhiteIp(ip)) {
            if (!likeLimiter.acquire(ip, stringRedisTemplate)) {
                result.setErrorMessage(BaseError.IP_TOO_FREQUENT);
                return result.toGsonResultString(cb);
            }
        }

        if (!TokenUtil.isTokenValid(username, token)) {
            result.setErrorMessage(BaseError.TOKEN_VOERDUE);
            return result.toGsonResultString(cb);
        }
        RedisLikeUtil.likeOrUnLike(redisLikeService,status,aid,username, RedisLikeUtil.MAP_KEY_USER_LIKED, RedisLikeUtil.MAP_KEY_USER_LIKED_COUNT);
        return result.toGsonResultString(cb);
    }

    /**
     * 评论（点赞/取消赞）
     */
    @RequestMapping(value = "/comment/like", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    public String commentLike(
            @RequestParam(value = "cid") Integer cid,
            @RequestParam(value = "username") String username,
            @RequestParam(value = "token") String token,
            @RequestParam(value = "status", defaultValue = "0") Integer status,
            @RequestParam("callback") String cb,HttpServletRequest request){
        CommonResultVO result = new CommonResultVO();

        String ip = HttpUtil.getIpAddr(request);

        //ip防刷
        if (!SimpleRateLimiter.isWhiteIp(ip)) {
            if (!likeLimiter.acquire(ip, stringRedisTemplate)) {
                result.setErrorMessage(BaseError.IP_TOO_FREQUENT);
                return result.toGsonResultString(cb);
            }
        }

        if (!TokenUtil.isTokenValid(username, token)) {
            result.setErrorMessage(BaseError.TOKEN_VOERDUE);
            return result.toGsonResultString(cb);
        }
        RedisLikeUtil.likeOrUnLike(redisLikeService,status,cid,username, RedisLikeUtil.COMMENT_KEY_USER_LIKED, RedisLikeUtil.COMMENT_KEY_USER_LIKED_COUNT);
        return result.toGsonResultString(cb);
    }

    /**
     * 留言列表
     */
    @RequestMapping(value = "/article/message/list", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    public String messageLike(@RequestParam("callback") String cb,HttpServletRequest request){
        CommonResultVO result = new CommonResultVO();
        String ip = HttpUtil.getIpAddr(request);

        //ip防刷
        if (!SimpleRateLimiter.isWhiteIp(ip)) {
            if (!pageVisitLimiter.acquire(ip, stringRedisTemplate)) {
                result.setErrorMessage(BaseError.IP_TOO_FREQUENT);
                return result.toGsonResultString(cb);
            }
        }
        result.put("result", userMessageService.getAll());
        return result.toGsonResultString(cb);
    }

    /**
     * 留言删除
     */
    @RequestMapping(value = "/article/message/del", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    public String delMessage(
            @RequestParam("callback") String cb,
            @RequestParam("id") String id,
            @RequestParam("userId") String userId,
            @RequestParam("username") String username,
            @RequestParam("token") String token,
            HttpServletRequest request){

        CommonResultVO result = new CommonResultVO();

        String ip = HttpUtil.getIpAddr(request);
        //ip防刷
        if (!SimpleRateLimiter.isWhiteIp(ip)) {
            if (!messageAddIpLimiter.acquire(ip, stringRedisTemplate)) {
                result.setErrorMessage(BaseError.IP_TOO_FREQUENT);
                return result.toGsonResultString(cb);
            }
        }

        if (!TokenUtil.isTokenValid(username, token)) {
            result.setErrorMessage(BaseError.TOKEN_VOERDUE);
            return result.toGsonResultString(cb);
        }
        if (userMessageService.deleteById(id, userId) == 0) {
            result.setErrorMessage("2", "我擦，手速有点快");
            return result.toGsonResultString(cb);
        }
        return result.toGsonResultString(cb);
    }

    /**
     * 留言添加
     */
    @RequestMapping(value = "/article/message/submit", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    public String submitMessage(
            @RequestParam("callback") String cb,
            @RequestParam(value = "content",required = false) String content,
            @RequestParam(value = "userId",required = false) Integer userId,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "token", required = false) String token,
            HttpServletRequest request){
        CommonResultVO result = new CommonResultVO();

        String ip = HttpUtil.getIpAddr(request);
        //ip防刷
        if (!SimpleRateLimiter.isWhiteIp(ip)) {
            if (!messageAddIpLimiter.acquire(ip, stringRedisTemplate)) {
                result.setErrorMessage(BaseError.IP_TOO_FREQUENT);
                return result.toGsonResultString(cb);
            }
        }

        if (!TokenUtil.isTokenValid(username, token)) {
            result.setErrorMessage(BaseError.TOKEN_VOERDUE);
            return result.toGsonResultString(cb);
        }
        if (userMessageService.save(userId, content) == 0) {
            result.setErrorMessage("2", "哎呀，提交失败了");
            return result.toGsonResultString(cb);
        }
        return result.toGsonResultString(cb);
    }
}
