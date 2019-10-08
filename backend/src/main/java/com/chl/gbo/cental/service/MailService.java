package com.chl.gbo.cental.service;

import com.chl.gbo.cental.bean.MailDO;

/**
 * @Auther: BoYanG
 * @Describe 邮件服务类
 */
public interface MailService {
    boolean sendTextMail(MailDO mailDO);

    void sendHtmlMail(MailDO mailDO,boolean isShowHtml);

    void sendTemplateMail(MailDO mailDO);
}
