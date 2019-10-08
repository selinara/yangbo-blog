package com.chl.gbo.cental.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.chl.gbo.cental.domain.Role;
import com.chl.gbo.cental.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @Auther: BoYanG
 * @Describe 实现UserDetails 用于封装数据库对象实例
 */
@NoArgsConstructor
public class UserDetailsDto implements UserDetails {

    private Integer userid;
    private String username;
    private String password;

    @Getter
    @Setter
    private List<Role> roles;

    public UserDetailsDto(User user){
        this.username = user.getLoginAccount();
        this.password = user.getLoginPass();
    }

    public UserDetailsDto(User user, List<Role> roles){
        this.userid = user.getUserId();
        this.username = user.getLoginAccount();
        this.password = user.getLoginPass();
        this.roles = roles;
    }

    //返回用户所有角色的封装，一个Role对应一个GrantedAuthority
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Role role: roles) {
            authorities.add(new SimpleGrantedAuthority(role.getRoleKey()));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    //判断账号是否已经过期，默认没有过期
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    //判断账号是否被锁定，默认没有锁定
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    //判断信用凭证是否过期，默认没有过期
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    //判断账号是否可用，默认可用
    @Override
    public boolean isEnabled() {
        return true;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public static Integer getCurrentUserId(Authentication authentication){
        UserDetailsDto userDetailsDto = (UserDetailsDto)authentication.getPrincipal();
        if (userDetailsDto==null)
            throw new RuntimeException("获取用户信息失败");
        return userDetailsDto.getUserid();
    }
}
