package com.chl.gbo.cental.enumclass;

import lombok.Getter;

/**
 * @Auther: BoYanG
 * @Describe 点赞状态
 */
@Getter
public enum UserLikeEnum {

    LIKE(1, "点赞"),
    UNLIKE(0, "取消点赞/未点赞"),
    ;

    private Integer code;
    private String msg;

    UserLikeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
