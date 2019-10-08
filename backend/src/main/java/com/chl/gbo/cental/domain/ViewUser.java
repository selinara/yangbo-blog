package com.chl.gbo.cental.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Auther: BoYanG
 * @Describe 用户表
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "tb_view_user")
public class ViewUser {
    @Id
    @GeneratedValue
    private Integer userId;
    private String ip;
    private String username;
    private String userpsw;
    private String email;
    private String headpic;
    private String userLevel;
    private String userRights;
    private String registerTime;
    private String birthday;
    private String phone;
    private String nickname;
    private Integer age;
    private Integer islock;//0:未锁定；1:已锁定

    public boolean lock(){
        return getIslock().equals(1);
    }
}
