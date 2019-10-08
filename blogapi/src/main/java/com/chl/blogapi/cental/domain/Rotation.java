package com.chl.blogapi.cental.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Auther: BoYanG
 * @Describe 滚轮操作
 */
@Data
@AllArgsConstructor
public class Rotation {

    private String content;

    private String url;

    private Boolean isFirst;

    private String siteSource;

    private String siteTitle;

}
