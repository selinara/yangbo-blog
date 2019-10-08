package com.chl.gbo.cental.bean;

import java.util.Map;

import lombok.Data;

/**
 * @Auther: BoYanG
 * @Describe 发送邮件
 */
@Data
public class MailDO {

    //标题
    private String title;
    //内容
    private String content;
    //接收人邮件地址
    private String email;
    //附加，value 文件的绝对地址/动态模板数据
    private Map<String, Object> attachment;

    public MailDO(String content, String email, String type) {
        this.title = type.equals("0")?"YanGBO博客_注册验证码":"YanGBO博客_修改密码验证码";
        this.content = "验证码："+content;
        this.email = email;
    }
}
