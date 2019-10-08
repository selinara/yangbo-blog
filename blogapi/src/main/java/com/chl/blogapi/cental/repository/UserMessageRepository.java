package com.chl.blogapi.cental.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.chl.blogapi.cental.domain.UserMessage;

/**
 * @Auther: BoYanG
 * @Describe 用户留言
 */
public interface UserMessageRepository  extends JpaRepository<UserMessage, Integer> {

    @Query(value = "SELECT a.*,b.headpic,b.nickname FROM tb_message a LEFT JOIN tb_view_user b ON a.userId = b.userId order by a.createTime desc", nativeQuery = true)
    List<UserMessage> findAllUserMessageInfo();

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM tb_message WHERE messageId = ?2 AND userId = ?1", nativeQuery = true)
    Integer deleteByUserIdAndId(String userId, String id);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "insert into tb_message(userId, content, createTime) values (?2,?1,now())", nativeQuery = true)
    Integer saveUserMessage(String content, Integer userId);
}
