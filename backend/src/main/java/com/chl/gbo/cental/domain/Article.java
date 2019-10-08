package com.chl.gbo.cental.domain;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.chl.gbo.util.DateUtil;
import com.chl.gbo.util.HtmlUtil;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Auther: BoYanG
 * @Describe 博文实体
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "tb_articles")
public class Article {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Integer articleId;

    private Integer userId;

    private Integer sortId;

    private String labelId;

    private String articleTitle;

    private String articleContent;

    private Integer articleCommentCount = 0;

    private Integer articleViews = 0;

    private Integer articleLikeCount = 0;

    private Integer isRelease = 0;

    private String articleDate = DateUtil.now();

    private String sortName;

    private String labelName;

    private Integer isRoof = 0;

    @Transient
    private List<String> labelNames;
    @Transient
    private String author;
    @Transient
    private String base64Img;
    @Transient
    private Integer isLike = 0;//当前用户是否已赞,默认未赞

    public void setLabelNamesByNames(String labelName) {
        setLabelNames(Arrays.asList(labelName.split(",")));
    }

    public void setBase64ImgByContent(String articleContent) {
        List<String> imgList = HtmlUtil.match(articleContent, "img","src");
        setBase64Img(imgList.size() == 0 ? "img/aticleview.png" : imgList.get(0));
    }
}
