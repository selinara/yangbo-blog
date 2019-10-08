package com.chl.blogapi.cental.vo;

import java.util.List;

import com.chl.blogapi.cental.domain.Article;

import lombok.Data;

/**
 * @Auther: BoYanG
 * @Describe 分类详情
 */
@Data
public class SortDetailVO {

    private List<Article> homeArticle;

    private List<Article> siteArticle;

    private Integer size;

    private Integer pagesize;

    private List<FriendShipLinkVO> friendShipLinkVOS;

}
