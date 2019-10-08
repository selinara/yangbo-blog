package com.chl.blogapi.cental.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Auther: BoYanG
 * @Describe 文章点赞数
 */
@Data
@AllArgsConstructor
public class LikedCountDTO {

    private Integer articleId;

    private Integer count;

}
