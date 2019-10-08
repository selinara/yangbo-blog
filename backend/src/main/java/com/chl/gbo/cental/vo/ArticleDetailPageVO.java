package com.chl.gbo.cental.vo;

import java.util.List;
import java.util.Map;

import com.chl.gbo.cental.domain.Article;

import lombok.Data;

/**
 * @Auther: BoYanG
 * @Describe 文章详情页
 */
@Data
public class ArticleDetailPageVO {

    private Article article;

    private List<PreviousNextVO> prenext;

    private List<Article> siteArticle;

    private Object friendShipLink;

    private List<FriendShipLinkVO> friendShipLinkVOS;

}
