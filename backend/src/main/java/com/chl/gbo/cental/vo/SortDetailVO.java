package com.chl.gbo.cental.vo;

import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import com.chl.gbo.cental.domain.Article;
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
