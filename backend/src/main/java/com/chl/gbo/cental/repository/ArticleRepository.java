package com.chl.gbo.cental.repository;

import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import com.chl.gbo.cental.domain.Article;

/**
 * @Auther: BoYanG
 * @Describe 博文
 */
public interface ArticleRepository extends JpaRepository<Article, Integer> {
    //-------------------------------------------------后台repo-------------------------------------------------------
    @Query(value = "SELECT a.articleId,a.userId,a.sortId,a.labelId,a.articleTitle,a.articleContent,a.articleViews," +
            " a.articleCommentCount,a.articleDate,a.articleLikeCount,a.isRelease,b.sortName,a.labelName,a.isRoof " +
            "FROM tb_articles a LEFT JOIN tb_sorts b ON a.sortId = b.sortId " +
            " WHERE  a.userId = ?1 AND IF(?2!='' , a.articleTitle LIKE ?2, 1=1)  AND IF(?3!=0 , a.sortId = ?3, 1=1)  AND  IF(?4!='' , a.labelId like ?4, 1=1)  " +
            "ORDER BY isRoof DESC,articleDate DESC LIMIT ?5, ?6", nativeQuery = true)
    List<Article> getArticleListByTitle(Integer userid, String articleTitle,Integer sortId, String labelId, Integer start, Integer size);

    @Query(value = "SELECT COUNT(1) FROM tb_articles a WHERE a.userId = ?2 AND IF(?1!='' , a.articleTitle LIKE ?1, 1=1) " +
            " AND IF(?3!=0 , a.sortId = ?3, 1=1)  AND  IF(?4!='' , a.labelId like ?4, 1=1)", nativeQuery = true)
    Integer countByTitle(String articleTitle, Integer userid, Integer sortId, String labelId);

    @Query(value = "SELECT a.*, NULL AS sortName,NULL AS labelName FROM tb_articles a WHERE a.articleId = ?1", nativeQuery = true)
    Article findListById(int i);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE tb_articles SET isRelease = ?2 WHERE articleId = ?1", nativeQuery = true)
    int status(Integer aid, Integer st);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "delete from tb_articles WHERE sortId = ?1", nativeQuery = true)
    void deleteBySortId(Integer sid);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "delete from tb_articles WHERE labelId like ?1", nativeQuery = true)
    void deleteArticleByLableId(String lid);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE tb_articles SET isRoof = CASE WHEN  isRoof = 1 THEN 0 ELSE 1 END WHERE isRoof = 1 OR articleId = ?1", nativeQuery = true)
    void roof(Integer aid);
}
