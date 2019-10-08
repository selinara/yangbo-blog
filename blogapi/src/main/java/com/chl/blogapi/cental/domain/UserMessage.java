package com.chl.blogapi.cental.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Auther: BoYanG
 * @Describe 用户留言
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "tb_message")
public class UserMessage {
    @Id
    @GeneratedValue
    private Integer messageId;
    private Integer userId;
    private String content;
    private String createTime;
    private String headpic;
    private String nickname;
}
