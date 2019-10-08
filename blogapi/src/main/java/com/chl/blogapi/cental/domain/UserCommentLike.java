package com.chl.blogapi.cental.domain;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "tb_comment_like")
public class UserCommentLike {
    @Id
    @GeneratedValue
    private Integer id;
    private String username;
    private Integer commentId;
    private Integer status;
    private Date createTime = new Date();

    public UserCommentLike(String username, Integer commentId, Integer status) {
        this.username = username;
        this.commentId = commentId;
        this.status = status;
    }
}
