package com.chl.blogapi.cental.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.thymeleaf.util.StringUtils;

import com.chl.blogapi.cental.domain.ViewUser;
import com.chl.blogapi.cental.repository.ViewUserRepository;
import com.chl.blogapi.cental.vo.ThirdPartyVO;
import com.chl.blogapi.cental.vo.ThirdUserInfo;

/**
 * @Auther: BoYanG
 * @Describe 三方登录操作
 */
public abstract class ThirdPartyOperation {

    public ThirdPartyVO thirdPartyVO;

    @Autowired
    protected ViewUserRepository viewUserRepository;

    protected abstract ThirdUserInfo getAccessToken(String code);

    protected abstract String getOpenId(ThirdUserInfo thirdUserInfo);

    protected abstract Object getUserInfo(ThirdUserInfo thirdUserInfo);

    protected abstract String biztype();

    protected abstract ViewUser saveThirdUser(Object thirdUser);

    /**
     * 公共服务类
     * @param code
     * @return
     */
    public Object getThirdUser(String code){
        ThirdUserInfo thirdUserInfo = getAccessToken(code);
        if (thirdUserInfo==null) {
            return null;
        }
        String openId = getOpenId(thirdUserInfo);
        if (!StringUtils.isEmpty(openId)) {
            thirdUserInfo.setOpenId(openId);
        }
        return getUserInfo(thirdUserInfo);
    }

    public void setThirdPartyVO(ThirdPartyVO thirdPartyVO) {
        this.thirdPartyVO = thirdPartyVO;
    }

    /**
     * 去除三方用户头像路径中的#
     * @param user
     * @param nickname
     * @param headpic
     * @param viewUserRepository
     * @return
     */
    protected static ViewUser updateUserInfo(ViewUser user, String nickname, String headpic, ViewUserRepository viewUserRepository){
        headpic = headpic.indexOf(",")!=-1?headpic.replaceAll(",", "#"):headpic;
        if(!user.getNickname().equals(nickname) || !user.getHeadpic().equals(headpic)){
            viewUserRepository.updateUser(user.getUserId(), nickname, headpic);
            user.setHeadpic(headpic);
            user.setNickname(nickname);
            return user;
        }
        return user;
    }

}
