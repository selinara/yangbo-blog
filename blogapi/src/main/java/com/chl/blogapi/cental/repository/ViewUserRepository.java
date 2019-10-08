package com.chl.blogapi.cental.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.chl.blogapi.cental.domain.ViewUser;

public interface ViewUserRepository extends JpaRepository<ViewUser, Integer> {

    @Query(value = "SELECT * FROM tb_view_user WHERE email = ?1", nativeQuery = true)
    List<ViewUser> findByEmail(String usermail);

    @Query(value = "SELECT GROUP_CONCAT(username) FROM tb_view_user", nativeQuery = true)
    String findAllUserNames();

    @Query(value = "SELECT username FROM tb_view_user WHERE  email =?1 LIMIT 1", nativeQuery = true)
    String getUserNameByEmail(String email);

    @Query(value = "SELECT * FROM tb_view_user WHERE username=?1 LIMIT 1", nativeQuery = true)
    ViewUser getViewUserByUserName(String loginname);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE tb_view_user SET nickname=?2, phone=?3 WHERE username = ?1", nativeQuery = true)
    void updateUserInfoByUserName(String username, String nickname, String phone);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE tb_view_user SET headpic=?2 WHERE username = ?1", nativeQuery = true)
    void updateUserHeadPic(String username, String newPath);

    @Query(value = "SELECT GROUP_CONCAT(email) FROM tb_view_user", nativeQuery = true)
    String findAllUserMails();

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE tb_view_user SET userpsw=?2 WHERE email = ?1", nativeQuery = true)
    void changePsw(String usermail, String password);

    @Query(value = "SELECT * FROM tb_view_user WHERE openId=?1 LIMIT 1", nativeQuery = true)
    ViewUser findViewUserByOpenId(String openId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE tb_view_user SET headpic=?3,nickname=?2 WHERE userId = ?1", nativeQuery = true)
    void updateUser(Integer id, String nickname, String headpic);
}
