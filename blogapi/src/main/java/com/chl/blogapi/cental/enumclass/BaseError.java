package com.chl.blogapi.cental.enumclass;

import lombok.Getter;

/**
 * @Auther: BoYanG
 * @Describe 错误提示
 */
@Getter
public enum BaseError {

    TOKEN_VOERDUE("100", "当前登录过期啦，请退出重新登录哦"),
    IP_TOO_FREQUENT("110", "访问过于频繁"),
    ;

    private String code;
    private String message;

    BaseError(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
