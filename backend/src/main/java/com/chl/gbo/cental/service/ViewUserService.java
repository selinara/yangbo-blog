package com.chl.gbo.cental.service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.chl.gbo.cental.aspect.annotation.Cache;
import com.chl.gbo.cental.aspect.annotation.CacheKey;
import com.chl.gbo.cental.domain.ViewUser;
import com.chl.gbo.cental.repository.ViewUserRepository;
import com.chl.gbo.util.ConstantUtil;
import com.chl.gbo.util.DateUtil;
import com.chl.gbo.util.Md5Util;

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
            ViewUser user = build(username, usermail,password,ipAddr,ConstantUtil.getDefaultHeadPic());
            //新用户注册
            viewUserRepository.save(user);
            return true;
        } catch (Exception e) {
            logger.error("用户注册异常", e);
            return false;
        }
    }

    private static ViewUser build(String username,String usermail, String password, String ipAddr, String headPic){
        ViewUser viewUser = new ViewUser();
        viewUser.setUsername(username);
        viewUser.setUserpsw(Md5Util.getMd5Password(password));
        viewUser.setIp(ipAddr);
        viewUser.setEmail(usermail);
        viewUser.setRegisterTime(DateUtil.now());
        viewUser.setIslock(0);
        viewUser.setHeadpic(headPic);
        viewUser.setNickname(username);
        return viewUser;
    }

    /**
     * 生成用户名
     * @return
     */
    private static String genereateUsername(String email) {
        Random random = new Random();
        int num = random.nextInt(9999) % (9999-1000+1) + 1000;
        return num + DigestUtils.md5Hex(email).substring(8, 24).toLowerCase();
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
