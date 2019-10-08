package com.chl.gbo.cental.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.chl.gbo.cental.domain.UserArticleLike;

public interface UserLikeRepository extends JpaRepository<UserArticleLike, Integer> {

    @Query(value = "SELECT * FROM tb_articles_like WHERE ip=?1 AND articleId=?2", nativeQuery = true)
    UserArticleLike getUserLikeByIpAndAid(String ip, Integer aid);
}
