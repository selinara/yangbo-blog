package com.chl.blogapi.cental.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Auther: BoYanG
 * @Describe user
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "tb_user")
public class User {
    @Id
    @GeneratedValue
    private Integer userId;
    private String loginAccount;
    private String loginPass;
    private String userName;
    private String userHead;
    private String userPhone;
    private String userEmail;
    private Integer userSex;
    private String userBirthday;
    private String registerTime;
    private String departmentKey;

    private String roleIds;
    private String roleNames;
}
