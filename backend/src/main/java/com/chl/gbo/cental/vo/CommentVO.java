package com.chl.gbo.cental.vo;
import java.util.List;

import lombok.Data;

/**
 * @Auther: BoYanG
 * @Describe 评论结果集
 */
@Data
public class CommentVO {

    private String id;
    private String img;
    private String uid;
    private String replyName;
    private String fuid;
    private String beReplyName;
    private String content;
    private Integer islike = 0;
    private Integer likenum;
    private String time;

    public void setTime(String time) {
        this.time = time.substring(0, time.length()-2);
    }

    List<CommentVO> replyBody;
}
