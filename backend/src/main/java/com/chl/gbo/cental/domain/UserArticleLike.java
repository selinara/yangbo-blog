package com.chl.gbo.cental.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Auther: BoYanG
 * @Describe 用户点赞记录表
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "tb_articles_like")
public class UserArticleLike {

    @Id
    @GeneratedValue
    private Integer id;
    private String ip;
    private Integer articleId;
    private Integer status;
    private Date createTime = new Date();

    public UserArticleLike(String ip, Integer articleId, Integer status) {
        this.ip = ip;
        this.articleId = articleId;
        this.status = status;
    }
}
