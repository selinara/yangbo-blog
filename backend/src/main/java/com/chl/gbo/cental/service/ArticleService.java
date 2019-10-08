package com.chl.gbo.cental.service;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;
import com.chl.gbo.cental.domain.Article;
import com.chl.gbo.cental.repository.ArticleRepository;
import com.chl.gbo.cental.threadpool.SynaOperationUtil;

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
    private StringRedisTemplate stringRedisTemplate;

    //首页加载最新的6篇文章
    public static final Integer HOME_NEW_RELEASE_ARTICLE_COUNT = 6;

    public Integer count(String articleTitle, Integer userid, Integer sortId, String labelId){
        return articleRepository.countByTitle(title(articleTitle), userid, sortId, labelid(labelId));
    }

    /**
     * 不为空则进行模糊查询
     */
    private static String title(String articleTitle){
        return StringUtils.isEmpty(articleTitle) ? "" : "%"+articleTitle+"%";
    }
    private static String labelid(String labelId){
        return "0".equals(labelId) || StringUtils.isEmpty(labelId) ? "" : "%,"+labelId+",%";
    }

    /**
     * 根据DataTable格式进行sql分页
     * @param articleTitle 标题检索
     * @param start 开始条数
     * @param length 展示条数
     * @return
     */
    public List<Article> getArticlePage(Integer userid, String articleTitle, Integer sortId, String labelId, Integer start, Integer length) {
        if (StringUtils.isEmpty(articleTitle)){
            articleTitle="";
        } else {
            articleTitle = "%"+articleTitle+"%";
        }
        if (!StringUtils.isEmpty(labelId) && !"0".equals(labelId)) {
            labelId = "%,"+labelId+",%";
        } else {
            labelId = "";
        }
        return articleRepository.getArticleListByTitle(userid,articleTitle,sortId, labelId, start,length);
    }

    public Article findById(int i) {
        return articleRepository.findListById(i);
    }

    public void deleteById(int i) {
        articleRepository.deleteById(i);
        SynaOperationUtil.deleteCacheByArticleId(i, stringRedisTemplate, false);
    }

    public void insert(Article article) {
        boolean clearAll = article.getArticleId()!=null?false:true;
        SynaOperationUtil.deleteCacheByArticleId(article.getArticleId(), stringRedisTemplate, clearAll);
        articleRepository.save(article);
    }

    public void status(Integer aid, Integer st) {
        articleRepository.status(aid, st);
        SynaOperationUtil.deleteCacheByArticleId(aid, stringRedisTemplate, false);
    }

    public void roof(Integer aid) {
        articleRepository.roof(aid);
        SynaOperationUtil.deleteCacheByArticleId(aid, stringRedisTemplate, true);
    }

}
