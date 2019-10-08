package com.chl.blogapi.cental.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.chl.blogapi.cental.domain.UserCommentLike;

public interface UserCommentRepository extends JpaRepository<UserCommentLike, Integer> {

    @Query(value = "SELECT * FROM tb_comment_like WHERE username=?1 AND commentId=?2", nativeQuery = true)
    UserCommentLike getCommentLikeByUserNameAndAid(String username, Integer aid);
}
