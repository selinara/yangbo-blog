package com.chl.gbo.cental.repository;

import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.chl.gbo.cental.domain.User;

/**
 * @Auther: BoYanG
 * @Describe
 */
public interface UserRepository extends JpaRepository<User, Integer> ,JpaSpecificationExecutor<User> {

    List<User> findByLoginAccount(String loginAccount);

    /**
     * 查询所有用户的信息以及权限
     * @return
     */
    @Query(value = "SELECT t.userId,t.loginAccount,t.loginPass,t.userName,t.userHead,t.userPhone,t.userEmail,t.userSex," +
            "t.userBirthday,t.registerTime,t.departmentKey,GROUP_CONCAT(t.roleKey) AS roleNames,GROUP_CONCAT(t.roleId) AS roleIds " +
            "FROM (SELECT a.*,c.roleKey, c.roleId  FROM tb_user a LEFT JOIN tb_user_role b ON a.userId = b.userId " +
            "LEFT JOIN tb_role c ON b.roleId = c.roleId ) AS t  WHERE IF(?1!='' , t.loginAccount LIKE ?1, 1=1) GROUP BY loginAccount,loginPass", nativeQuery = true)
    List<User> findAllUsersAndAuthority(String loginAccount);

    @Modifying
    @Query(value = "delete from tb_user_role where userId =?1", nativeQuery = true)
    void deleteUserRole(Integer userId);

    @Modifying
    @Query(value = "insert into tb_user_role values (?1,?2)", nativeQuery = true)
    void addUserRole(Integer userId, Integer id);

}
