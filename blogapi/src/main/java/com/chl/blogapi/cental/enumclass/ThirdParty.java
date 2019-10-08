package com.chl.blogapi.cental.enumclass;

import lombok.Getter;

/**
 * @Auther: BoYanG
 * @Describe 三方平台
 */
@Getter
public enum ThirdParty {
    QQ("qq", "/chlsq/api/qq/qqLogin"),// QQ
    WB("wb", "/chlsq/api/wb/wbLogin"),// 微博
    ;

    private String code;
    private String uri;

    ThirdParty(String code, String uri) {
        this.code = code;
        this.uri = uri;
    }
}
