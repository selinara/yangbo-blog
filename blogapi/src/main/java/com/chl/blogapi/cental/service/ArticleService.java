package com.chl.blogapi.cental.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.chl.blogapi.cental.aspect.annotation.Cache;
import com.chl.blogapi.cental.aspect.annotation.CacheKey;
import com.chl.blogapi.cental.domain.Article;
import com.chl.blogapi.cental.domain.Labels;
import com.chl.blogapi.cental.domain.Rotation;
import com.chl.blogapi.cental.domain.SystemConstant;
import com.chl.blogapi.cental.repository.ArticleRepository;
import com.chl.blogapi.cental.repository.LabelRepository;
import com.chl.blogapi.cental.repository.UserRepository;
import com.chl.blogapi.cental.threadpool.SynaOperationUtil;
import com.chl.blogapi.cental.vo.ArticleDetailPageVO;
import com.chl.blogapi.cental.vo.CommentVO;
import com.chl.blogapi.cental.vo.FriendShipLinkVO;
import com.chl.blogapi.cental.vo.PreviousNextVO;
import com.chl.blogapi.cental.vo.SortDetailVO;
import com.chl.blogapi.util.DateUtil;
import com.chl.blogapi.util.HttpClientUtil;
import com.chl.blogapi.util.MapUtil;
import com.chl.blogapi.util.PageUtil;
import com.chl.blogapi.util.RedisLikeUtil;
import com.google.common.collect.Maps;

/**
 * @Auther: BoYanG
 * @Describe 博文
 */
@Service
public class ArticleService {

    private static final Log logger = LogFactory.getLog(ArticleService.class);

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisLikeService redisLikeService;

    @Autowired
    private SystemConstantService systemConstantService;

    //常量key
    private static final String FRIENDSHIPLINK = "friendlinks";

    /**
     * api
     * 加载首页
     */

    @Cache(prefix = "homePageArticleList",time = 30,unit = TimeUnit.DAYS)
    public List<Article> getHomePageArticleList(@CacheKey Integer n){
        List<Article> arts = articleRepository.getHomePageArticleList(n);
        return RedisLikeUtil.updateArticlePubInfo(arts, null);
    }

    // 置顶文章
    @Cache(prefix = "homePageTopArticle",time = 30,unit = TimeUnit.DAYS)
    public Article getHomePageTopArticle() {
        return articleRepository.getHomePageTopArticle();
    }
    // 热门标签
    @Cache(prefix = "hotTags",time = 30,unit = TimeUnit.DAYS)
    public List<Labels> hotTag() {
        return labelRepository.getHoteTags();
    }

    // 最新发布，右侧列表
    @Cache(prefix = "siteRelease",time = 30,unit = TimeUnit.DAYS)
    public List<Article> getRealeaseForm(){
        return articleRepository.getRealeaseForm();
    }

    /**
     * 文章详情页
     * @param id
     * @return
     */
    @Cache(prefix = "articleById",time = 30,unit = TimeUnit.DAYS)
    public ArticleDetailPageVO getArticleDetailById(@CacheKey Integer id) throws Exception{
        ArticleDetailPageVO vo = new ArticleDetailPageVO();
        Article article = articleRepository.findById(id).get();
        article.setAuthor(userRepository.findById(article.getUserId()).get().getLoginAccount());
        vo.setArticle(article);
        vo.setPrenext(MapUtil.listMapToListBean(articleRepository.findPreviousNext(id), PreviousNextVO.class));
        vo.setSiteArticle(getRealeaseForm());
        vo.setFriendShipLink(getFriendShipLinks());
        return vo;
    }

    /**
     * 分类详情
     * @param sid
     * @param pid
     * @return
     */
    @Cache(prefix = "articleDetailBySortIdAndPageId",time = 30,unit = TimeUnit.DAYS)
    public SortDetailVO getSortListPageDetail(@CacheKey Integer sid, @CacheKey Integer pid) {
        SortDetailVO result = new  SortDetailVO();
        List<Article> articles = articleRepository.getArticleListById(sid, PageUtil.getPageNo(pid));
        result.setHomeArticle(RedisLikeUtil.updateArticlePubInfo(articles, userRepository));
        result.setSiteArticle(getRealeaseForm());
        Integer size = articleRepository.getSizeBySortId(sid);
        result.setSize(size);
        result.setPagesize(PageUtil.getPageSize(size));
        result.setFriendShipLinkVOS(getFriendShipLinks());
        return result;
    }

    /**
     * 标签详情
     * @param id
     * @param pid
     * @return
     */
    @Cache(prefix = "articleDetailByTagAndPageId",time = 30,unit = TimeUnit.DAYS)
    public SortDetailVO getTagListPageDetail(@CacheKey Integer id, @CacheKey Integer pid) {
        SortDetailVO result = new  SortDetailVO();
        List<Article> articles = articleRepository.getArticleListByTagId(id, PageUtil.getPageNo(pid));
        result.setHomeArticle(RedisLikeUtil.updateArticlePubInfo(articles, userRepository));
        result.setSiteArticle(getRealeaseForm());
        result.setFriendShipLinkVOS(getFriendShipLinks());
        Integer size = articleRepository.getSizeByTagId(id);
        result.setSize(size);
        result.setPagesize(PageUtil.getPageSize(size));
        return result;
    }

    @Transactional
    public void transViewCountFromRedisDB() {
        Set<String> sets = stringRedisTemplate.keys(RedisLikeUtil.ARTICLE_VIEW_TIMES);
        stringRedisTemplate.delete(sets);
        for (String key : sets) {
            Integer inc = Integer.parseInt(stringRedisTemplate.opsForValue().get(key));
            Integer articleId = Integer.parseInt(key.split("_")[1]);
            articleRepository.updateViewByAid(articleId, inc);
        }
    }

    private static final String CIBA_CONTENT_URL = "http://sentence.iciba.com/index.php";

    /**
     * 词霸接口调用
     * @return
     */
    @Cache(prefix = "currentRotaions", time = 12, unit = TimeUnit.HOURS)
    public List<Rotation> getRotations() {
        Map pMap = Maps.newHashMap();
        pMap.put("c", "dailysentence");
        pMap.put("m", "getdetail");
        List<Rotation> result = new ArrayList<>();
        try {
            List<String> lastFourDays = DateUtil.lastFourDays();
            for (int i=0; i<lastFourDays.size(); i++) {
                pMap.put("title", lastFourDays.get(i));
                String response = HttpClientUtil.doGet(CIBA_CONTENT_URL, pMap, "utf-8");
                if (StringUtils.isEmpty(response)) {
                    throw new RuntimeException("词霸接口未获得数据。");
                }
                JSONObject res = JSONObject.parseObject(response);
                if (res.getInteger("errno")!=0) throw new RuntimeException("词霸接口未获得数据。");
                result.add(i, new Rotation(res.getString("content"), res.getString("picture2"), i==0?true:false,
                        res.getString("note"), res.getString("title")));
            }
            if(result.size() == 0){
                throw new RuntimeException("词霸接口未获得数据。");
            }
        } catch (Exception e){
            logger.info("调用词霸接口获取滚动资源出现异常，载入本地滚动资源");
            initLocalResource(result);
            return result;
        }
        return result;
    }

    private static List<Rotation> initLocalResource(List<Rotation> result){
        String day = DateUtil.nowDay();
        result.add(new Rotation("Keep on going never give up.", "img/slider/slide1.jpg",
                true,"有些人能清楚地听到自己内心深处的声音，并以此行事。这些人要么变成了疯子，要么成为传奇。", day));
        result.add(new Rotation("Kings have long arms.", "img/slider/slide2.jpg",
                false,"有些人能清楚地听到自己内心深处的声音，并以此行事。这些人要么变成了疯子，要么成为传奇。", day));
        result.add(new Rotation("Life is not all roses.", "img/slider/slide3.jpg",
                false,"有些人能清楚地听到自己内心深处的声音，并以此行事。这些人要么变成了疯子，要么成为传奇。", day));
        result.add(new Rotation("Being brave means knowing that when you fail, you don’t fail forever.", "img/slider/slide4.jpg",
                false,"有些人能清楚地听到自己内心深处的声音，并以此行事。这些人要么变成了疯子，要么成为传奇。", day));
        return result;
    }

    @Cache(prefix = "friendShipLinks", time = 20, unit = TimeUnit.DAYS)
    public List<FriendShipLinkVO> getFriendShipLinks() {
        SystemConstant constant = systemConstantService.getSystemConstantByKey(FRIENDSHIPLINK);
        if(constant==null){
            throw new RuntimeException("获取常量友情链接常量有误");
        }
        return JSON.parseObject(constant.getValue(), new TypeReference<List<FriendShipLinkVO>>(){});
    }

    public SortDetailVO getContentListPageDetail(String content, Integer pid) {
        SortDetailVO result = new  SortDetailVO();
        List<Article> articles = articleRepository.getArticleListBySearchContent(content, PageUtil.getPageNo(pid));
        result.setHomeArticle(RedisLikeUtil.updateArticlePubInfo(articles, userRepository));
        result.setSiteArticle(getRealeaseForm());
        result.setFriendShipLinkVOS(getFriendShipLinks());
        Integer size = articleRepository.getSizeByContent(content);
        result.setSize(size);
        result.setPagesize(PageUtil.getPageSize(size));
        return result;
    }

    public List<Map> getPersonFilingList(){
        return articleRepository.getPersonFilingList();
    }

    /**
     * 获得文章评论列表
     * @param articleId
     * @param username
     * @return
     */
    public List<CommentVO> getCommentListByArticleId(String articleId, String username) {
        List<CommentVO> result = new ArrayList<>();
        List<Map> commenList = articleRepository.getCommentListById(articleId);
        for (Map map : commenList) {
            CommentVO co = null;
            for (CommentVO commentVO : result) {
                if (commentVO.getId().equals(map.get("cid").toString())) {
                    co = commentVO;
                    break;
                }
            }
            if (co != null) {
                co.setReplyBody(commentVOList(co.getReplyBody(), map));
            } else {
                co = new CommentVO();
                co.setId(map.get("cid").toString());
                co.setImg(map.get("headpic").toString());
                co.setContent(map.get("content").toString());
                co.setReplyName(map.get("nick").toString());
                co.setTime(map.get("cdate").toString());
                co.setUid(map.get("uid").toString());
                co.setLikenum(Integer.parseInt(map.get("likenum").toString()));
                if (!StringUtils.isEmpty(username)) {
                    co = updateLikeNumAndStatus(co, Integer.parseInt(map.get("cid").toString()), username);
                }
                co.setReplyBody(commentVOList(new ArrayList<>(), map));
                result.add(co);
            }
        }
        return result;
    }

    private static List<CommentVO> commentVOList(List<CommentVO> sonList, Map map){
        if (map.get("sid") != null) {
            CommentVO svo = new CommentVO();
            svo.setId(map.get("sid").toString());
            svo.setReplyName(map.get("snick").toString());
            svo.setUid(map.get("suid").toString());
            svo.setBeReplyName(map.get("fnick").toString());
            svo.setFuid(map.get("fuid").toString());
            svo.setContent(map.get("scontent").toString());
            svo.setTime(map.get("sdate").toString());
            sonList.add(svo);
        }
        return sonList;
    }

    //更新评论点赞数量以及状态
    private CommentVO updateLikeNumAndStatus(CommentVO co, Integer cid, String username) {

        //点赞数
        Object redisVal = redisLikeService.getObjectFromRedis(RedisLikeUtil.getLikedKey(cid, username), RedisLikeUtil.COMMENT_KEY_USER_LIKED);
        Integer status = articleRepository.getIsLikeByUserName(username, cid);
        if (redisVal != null) {
            co.setIslike((Integer) redisVal);
            Integer likeCount = co.getLikenum();
            if (status != null && !status.equals(Integer.parseInt(redisVal.toString()))) {
                co.setLikenum((Integer) redisVal == 1 ? likeCount + 1 : (likeCount - 1 < 0 ? 0 : likeCount - 1));
            }
            if (status == null && (Integer) redisVal == 1) {
                co.setLikenum(likeCount + 1);
            }
            return co;
        }

        //点赞状态
        if (status != null) co.setIslike(status);
        return co;
    }

    @Transactional
    public void addArticleComment(Integer articleId, Integer userId, Integer foruserId,Integer parentCommentId, String content) {

        articleRepository.addArticleComment(articleId, userId, foruserId, parentCommentId, content);
        //更新文章评论数量
        articleRepository.changeArticleCommentNum(articleId, 1);
        //清除缓存
        SynaOperationUtil.deleteCacheByArticleId(articleId, stringRedisTemplate, false);
    }

    @Transactional
    public void delComment(Integer commentId, Integer parentCommentId, Integer articleId) {
        //更新文章评论数量
        articleRepository.changeArticleCommentNum(articleId, -1);
        //清除缓存
        SynaOperationUtil.deleteCacheByArticleId(articleId, stringRedisTemplate, false);
        if (parentCommentId==null) {
            articleRepository.deleteCommentById(commentId);
            articleRepository.deleteSonCommentByParentId(commentId);
            return;
        }
        articleRepository.deleteCommentById(commentId);
    }

    public List<Map> centerComment(Integer userId) {
        return articleRepository.centerComment(userId);
    }
}
