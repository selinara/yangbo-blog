package com.chl.blogapi.cental.service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chl.blogapi.cental.aspect.annotation.Cache;
import com.chl.blogapi.cental.aspect.annotation.CacheKey;
import com.chl.blogapi.cental.domain.ViewUser;
import com.chl.blogapi.cental.enumclass.ThirdParty;
import com.chl.blogapi.cental.repository.ViewUserRepository;
import com.chl.blogapi.cental.vo.QQUserInfo;
import com.chl.blogapi.cental.vo.WeiBoUserInfoVO;
import com.chl.blogapi.util.CommonUtil;
import com.chl.blogapi.util.ConstantUtil;
import com.chl.blogapi.util.DateUtil;
import com.chl.blogapi.util.Md5Util;

/**
 * @Auther: BoYanG
 */
@Service
public class ViewUserService {

    private static final Log logger = LogFactory.getLog(ViewUserService.class);

    @Autowired
    private ViewUserRepository viewUserRepository;

    public void insert(ViewUser viewUser){
        viewUserRepository.save(viewUser);
    }

    public boolean saveNewMailUser(String username,String usermail, String password, String ipAddr) {
        try{
            List<ViewUser> vList = viewUserRepository.findByEmail(usermail);
            ViewUser viewUser = vList.size()>0?vList.get(0):null;
            if (viewUser!=null) {
                viewUser.setUserpsw(Md5Util.getMd5Password(password));
                viewUser.setIp(ipAddr);
                viewUser.setUsername(username);
                //重置密码
                viewUserRepository.save(viewUser);
                return true;
            }
            ViewUser user = CommonUtil.build(username, usermail,password,ipAddr,ConstantUtil.getDefaultHeadPic(),  null, null, null);
            //新用户注册
            viewUserRepository.save(user);
            return true;
        } catch (Exception e) {
            logger.error("用户注册异常", e);
            return false;
        }
    }

    public String findAllUserNames() {
        return viewUserRepository.findAllUserNames();
    }
    public String findAllUserMails() {
        return viewUserRepository.findAllUserMails();
    }

    /**
     * 根据邮箱查询用户名
     * @param email
     * @return
     */
    public String getUserNameByEmail(String email) {
        return viewUserRepository.getUserNameByEmail(email);
    }

    @Cache(prefix = "UserInfo", time = 30, unit = TimeUnit.DAYS)
    public ViewUser getViewUserByUserName(@CacheKey String loginname) {
        return viewUserRepository.getViewUserByUserName(loginname);
    }

    public void updateUserInfoByUserName(String username, String nickname, String phone) {
        viewUserRepository.updateUserInfoByUserName(username, nickname, phone);
    }

    public void updateUserHeadPic(String username, String newPath) {
        viewUserRepository.updateUserHeadPic(username,newPath);
    }

    public boolean changePsw(String usermail, String password) {
        try {
            viewUserRepository.changePsw(usermail, password);
        } catch (Exception e) {
            logger.error("change password occurs error,email="+usermail, e);
            return false;
        }
        return true;
    }
}
