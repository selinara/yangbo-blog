package com.chl.blogapi.cental.service.impl;

import com.chl.blogapi.cental.domain.ViewUser;
import com.chl.blogapi.cental.enumclass.ThirdParty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.chl.blogapi.cental.service.ThirdPartyOperation;
import com.chl.blogapi.cental.service.ViewUserService;
import com.chl.blogapi.cental.vo.QQUserInfo;
import com.chl.blogapi.cental.vo.ThirdUserInfo;
import com.chl.blogapi.util.CommonUtil;
import com.chl.blogapi.util.ConstantUtil;
import com.chl.blogapi.util.HttpClientUtil;

/**
 * @Auther: BoYanG
 * QQ
 */
@Service
public class QQThirdPartyServiceImpl extends ThirdPartyOperation {

    private static final Log logger = LogFactory.getLog(QQThirdPartyServiceImpl.class);

    @Override
    protected ThirdUserInfo getAccessToken(String code) {
        ThirdUserInfo thirdUserInfo = new ThirdUserInfo();
        String authorzedUrl = String.format(thirdPartyVO.getAccessTokenUrl(), thirdPartyVO.getAppKey(), thirdPartyVO.getAppSecret(), code, thirdPartyVO.getRedirectUri());
        try{
            String res = HttpClientUtil.doGet(authorzedUrl, "UTF-8");
            logger.info("get qq access_token result:" + res);
            String[] items = StringUtils.splitByWholeSeparatorPreserveAllTokens(res, "&");
            String accessToken = StringUtils.substringAfterLast(items[0], "=");
            Long expiresIn = new Long(StringUtils.substringAfterLast(items[1], "="));
            String refreshToken = StringUtils.substringAfterLast(items[2], "=");
            //token信息
            thirdUserInfo.setAccessToken(accessToken);
            thirdUserInfo.setExpiresIn(String.valueOf(expiresIn));
            thirdUserInfo.setRefreshToken(refreshToken);
        }catch (Exception e) {
            logger.info("get qq access_token occurs error " + e.getMessage());
            return null;
        }
        return thirdUserInfo;
    }

    @Override
    protected String getOpenId(ThirdUserInfo thirdUserInfo) {
        String openidUrl = String.format(thirdPartyVO.getOpenIdUrl(), thirdUserInfo.getAccessToken());
        try {
            String result = HttpClientUtil.doGet(openidUrl, "UTF-8");
            return StringUtils.substringBetween(result, "\"openid\":\"", "\"}");
        }catch (Exception e) {
            logger.info("get qq openId occurs error" + e.getMessage());
        }
        return null;
    }

    @Override
    protected Object getUserInfo(ThirdUserInfo thirdUserInfo) {
        String userinfoUrl = String.format(thirdPartyVO.getUserInfoUrl(), thirdUserInfo.getAccessToken(),
                thirdPartyVO.getAppKey(), thirdUserInfo.getOpenId());
        try {
            String result = HttpClientUtil.doGet(userinfoUrl, "UTF-8");
            QQUserInfo user = JSON.parseObject(result, QQUserInfo.class);
            user.setOpenId(thirdUserInfo.getOpenId());
            return user;
        }catch (Exception e) {
            logger.info("get qq userinfo occurs error" + e.getMessage());
        }
        return null;
    }

    @Override
    protected String biztype() {
        return ThirdParty.QQ.getCode();
    }

    @Override
    protected ViewUser saveThirdUser(Object thirdUser) {
        ViewUser newQqUser = new ViewUser();
        if(thirdUser instanceof QQUserInfo){
            QQUserInfo qqUser = (QQUserInfo) thirdUser;
            ViewUser user = viewUserRepository.findViewUserByOpenId(qqUser.getOpenId());
            String nickname = qqUser.getNickname();
            String headpic = qqUser.getFigureurl_2();
            if(user!=null){
                return updateUserInfo(user, nickname, headpic, viewUserRepository);
            }
            newQqUser = CommonUtil.build(CommonUtil.genereateUsername(qqUser.getOpenId(), ThirdParty.QQ.getCode()), null,
                    ConstantUtil.defaultPassword(),null, headpic, qqUser.getOpenId(),nickname, ThirdParty.QQ.getCode());
            viewUserRepository.save(newQqUser);
        }
        return newQqUser;
    }
}
