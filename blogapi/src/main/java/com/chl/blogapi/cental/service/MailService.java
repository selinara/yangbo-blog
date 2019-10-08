package com.chl.blogapi.cental.service;

import com.chl.blogapi.cental.bean.MailDO;

/**
 * @Auther: BoYanG
 * @Describe 邮件服务类
 */
public interface MailService {
    boolean sendTextMail(MailDO mailDO);

    void sendHtmlMail(MailDO mailDO, boolean isShowHtml);

    void sendTemplateMail(MailDO mailDO);
}
