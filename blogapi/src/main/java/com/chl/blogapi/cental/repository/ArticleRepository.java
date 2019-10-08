package com.chl.blogapi.cental.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.chl.blogapi.cental.domain.Article;

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
    List<Article> getArticleListByTitle(Integer userid, String articleTitle, Integer sortId, String labelId, Integer start, Integer size);

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

    //-------------------------------------------------前台repo-------------------------------------------------------
    @Query(value = "SELECT * FROM (SELECT * FROM tb_articles where isRelease = 1 ORDER BY articleDate DESC) AS t  LIMIT ?1", nativeQuery = true)
    List<Article> getHomePageArticleList(Integer n);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE tb_articles SET isRoof = CASE WHEN  isRoof = 1 THEN 0 ELSE 1 END WHERE isRoof = 1 OR articleId = ?1", nativeQuery = true)
    void roof(Integer aid);

    @Query(value = "SELECT * FROM tb_articles where isRoof = 1 limit 1", nativeQuery = true)
    Article getHomePageTopArticle();

    @Query(value = "SELECT * FROM tb_articles WHERE isRelease = 1 ORDER BY articleDate DESC LIMIT 10", nativeQuery = true)
    List<Article> getRealeaseForm();

    @Query(value = "SELECT DISTINCT  w,id, b.articletitle AS title FROM" +
            "(SELECT '0' AS w, MAX(a.articleId) AS id  FROM tb_articles a WHERE a.isRelease = 1 AND a.articleId < ?1 UNION ALL " +
            "SELECT  '1' AS w, MIN(c.articleId) AS id  FROM tb_articles c WHERE c.isRelease = 1 AND c.articleId > ?1 UNION ALL " +
            "SELECT '0' AS w, MAX(b.articleId) AS id FROM tb_articles b WHERE b.isRelease = 1  AND b.articleId !=?1 UNION ALL " +
            "SELECT  '1' AS w, MIN(d.articleId) AS id  FROM tb_articles d WHERE d.isRelease = 1 AND d.articleId !=?1) AS t " +
            "LEFT JOIN tb_articles b ON t.id=b.articleId  WHERE t.id IS NOT NULL LIMIT 0,2;", nativeQuery = true)
    List<Map<String, Object>> findPreviousNext(Integer id);

    @Query(value = "SELECT * FROM tb_articles WHERE sortId=?1 AND isRelease = 1 ORDER BY articleDate DESC LIMIT ?2,3", nativeQuery = true)
    List<Article> getArticleListById(Integer sid, Integer pageNo);

    @Query(value = "SELECT * FROM tb_articles WHERE labelId like CONCAT('%,',CONCAT(?1, ',%'))  AND isRelease = 1 ORDER BY articleDate DESC LIMIT ?2,3", nativeQuery = true)
    List<Article> getArticleListByTagId(Integer id, Integer pageNo);

    @Query(value = "SELECT COUNT(1) FROM tb_articles WHERE sortId=?1 AND isRelease = 1", nativeQuery = true)
    Integer getSizeBySortId(Integer sid);

    @Query(value = "SELECT COUNT(1) FROM tb_articles WHERE labelId like CONCAT('%,',CONCAT(?1, ',%')) AND isRelease = 1", nativeQuery = true)
    Integer getSizeByTagId(Integer sid);

    @Query(value = "UPDATE tb_articles SET articleViews = articleViews + ?2 WHERE articleId = ?1", nativeQuery = true)
    void updateViewByAid(Integer articleId, Integer inc);

    @Query(value = "SELECT * FROM tb_articles WHERE articleTitle like CONCAT('%',CONCAT(?1, '%'))  AND isRelease = 1 LIMIT ?2,3", nativeQuery = true)
    List<Article> getArticleListBySearchContent(String content, Integer pageNo);

    @Query(value = "SELECT COUNT(1) FROM tb_articles WHERE articleTitle like CONCAT('%',CONCAT(?1, '%')) AND isRelease = 1", nativeQuery = true)
    Integer getSizeByContent(String content);

    @Query(value = "SELECT articleDate as mon,articleId,articleTitle  FROM tb_articles ORDER BY articleDate DESC", nativeQuery = true)
    List<Map> getPersonFilingList();

    @Query(value = "SELECT " +
            " a.commentId AS cid, " +
            " a.userId AS uid, " +
            " u1.nickname AS nick, " +
            " u1.headpic AS headpic, " +
            " a.commentLikeCount AS likenum, " +
            " a.commentContent AS content, " +
            " a.commentDate AS cdate, " +
            " b.commentId AS sid, " +
            " b.userId AS suid, " +
            " u2.nickname AS snick, " +
            " b.foruserId AS fuid, " +
            " u3.nickname AS fnick, " +
            " b.commentContent AS scontent, " +
            " b.commentDate AS sdate " +
            " FROM tb_comments a LEFT JOIN tb_comments b ON a.commentId = b.parentCommentId  " +
            "            LEFT JOIN tb_view_user u1 ON a.userId = u1.userId " +
            " LEFT JOIN tb_view_user u2 ON b.userId = u2.userId " +
            "LEFT JOIN tb_view_user u3 ON b.foruserId = u3.userId " +
            "WHERE  (a.articleId = ?1 AND b.articleId = ?1) OR (a.parentCommentId = 0 AND a.articleId=?1) " +
            "ORDER BY a.commentDate DESC ,b.commentDate ASC", nativeQuery = true)
    List<Map> getCommentListById(String articleId);

    @Query(value = "SELECT STATUS FROM tb_comment_like WHERE username = ?1 AND commentId = ?2 limit 1", nativeQuery = true)
    Integer getIsLikeByUserName(String username, Integer cid);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "INSERT INTO tb_comments (userId, foruserId, articleId, commentLikeCount,commentDate, commentContent, parentCommentId) " +
            "VALUES(?2, ?3, ?1, 0, NOW(), ?5, ?4)", nativeQuery = true)
    void addArticleComment(Integer articleId, Integer userId, Integer foruserId, Integer parentCommentId, String content);


    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM tb_comments WHERE commentId = ?1", nativeQuery = true)
    void deleteCommentById(Integer commentId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM tb_comments WHERE parentCommentId = ?1", nativeQuery = true)
    void deleteSonCommentByParentId(Integer commentId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE tb_articles SET articleCommentCount = articleCommentCount + ?2 WHERE articleId=?1", nativeQuery = true)
    void changeArticleCommentNum(Integer articleId, int i);

    @Query(value = "SELECT *  FROM tb_comments where commentId=?1", nativeQuery = true)
    List<Map> getCommentsById(Integer commentId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE tb_comments set commentLikeCount  = ?1 WHERE commentId = ?2", nativeQuery = true)
    void updateLikeCountById(Integer newNum, Integer commentId);

    @Query(value = "SELECT a.commentId, a.articleId, DATE_FORMAT(a.commentDate, '%Y年%m月%d日 %T') AS commentDate, b.nickname, a.commentContent,b.headpic " +
            "FROM tb_comments a LEFT JOIN tb_view_user b ON a.userId = b.userId  " +
            "WHERE a.foruserId = ?1 AND a.commentId NOT IN (SELECT parentCommentId FROM tb_comments WHERE parentCommentId != 0) " +
            "ORDER BY commentDate DESC LIMIT 10;", nativeQuery = true)
    List<Map> centerComment(Integer userId);
}
