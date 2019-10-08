package com.chl.blogapi.cental.vo;

import java.util.List;

import com.chl.blogapi.cental.domain.Article;
import com.chl.blogapi.cental.domain.Labels;
import com.chl.blogapi.cental.domain.Rotation;

import lombok.Data;

/**
 * @Auther: BoYanG
 * @Describe 首页
 */
@Data
public class HomePageVO {

    private List<Rotation> rotations;

    private Object siteNotice;

    private List<Labels> hotTags;

    private Article topArticle;

    private List<Article> homeArticle;

    private List<Article> siteArticle;

    private List<FriendShipLinkVO> friendShipLink;

}
