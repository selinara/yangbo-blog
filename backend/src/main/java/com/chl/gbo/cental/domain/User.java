package com.chl.gbo.cental.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
