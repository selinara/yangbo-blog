package com.chl.blogapi.util;

import java.util.Random;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.chl.blogapi.cental.domain.ViewUser;

/**
 * @Auther: BoYanG
 */
public class CommonUtil {

    /**
     * 用户实体
     * @param username
     * @param usermail
     * @param password
     * @param ipAddr
     * @param headPic
     * @param openId
     * @param nickname
     * @param biztype
     * @return
     */
    public static ViewUser build(String username, String usermail, String password,
                                 String ipAddr, String headPic, String openId, String nickname, String biztype){
        ViewUser viewUser = new ViewUser();
        viewUser.setUsername(username);
        viewUser.setUserpsw(Md5Util.getMd5Password(password));
        viewUser.setIp(ipAddr);
        viewUser.setEmail(usermail);
        viewUser.setRegisterTime(DateUtil.now());
        viewUser.setIslock(0);
        viewUser.setHeadpic(headPic.indexOf(",")!=-1?headPic.replaceAll(",", "#"):headPic);
        viewUser.setNickname(StringUtils.isEmpty(nickname)?username:nickname);
        viewUser.setOpenId(openId);
        viewUser.setBiztype(biztype);
        return viewUser;
    }

    /**
     * 生成用户名
     * @return
     */
    public static String genereateUsername(String openId, String type) {
        Random random = new Random();
        int num = random.nextInt(9999) % (9999-1000+1) + 1000;
        return "_" + num + DigestUtils.md5Hex(openId).substring(8, 24).toLowerCase() + "@" + type;
    }


}
