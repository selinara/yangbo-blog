package com.chl.blogapi.cental.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Auther: BoYanG
 * @Describe 三方登录公共信息
 */
@Data
@NoArgsConstructor
public class ThirdUserInfo {

    private String code;
    private String accessToken;
    private String openId;
    private String expiresIn;
    private String refreshToken;

}
