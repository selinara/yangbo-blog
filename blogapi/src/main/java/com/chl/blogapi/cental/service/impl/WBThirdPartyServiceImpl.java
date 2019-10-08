package com.chl.blogapi.cental.service.impl;

import java.util.HashMap;
import java.util.Map;

import com.chl.blogapi.cental.domain.ViewUser;
import com.chl.blogapi.cental.enumclass.ThirdParty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chl.blogapi.cental.service.ThirdPartyOperation;
import com.chl.blogapi.cental.vo.ThirdUserInfo;
import com.chl.blogapi.cental.vo.WeiBoUserInfoVO;
import com.chl.blogapi.util.CommonUtil;
import com.chl.blogapi.util.ConstantUtil;
import com.chl.blogapi.util.HttpClientUtil;

/**
 * @Auther: BoYanG
 * 微博
 */
@Service
public class WBThirdPartyServiceImpl extends ThirdPartyOperation {

    private static final Log logger = LogFactory.getLog(WBThirdPartyServiceImpl.class);

    @Override
    protected ThirdUserInfo getAccessToken(String code) {
        String tokenUrl = String.format(thirdPartyVO.getAccessTokenUrl(), thirdPartyVO.getAppKey(), thirdPartyVO.getAppSecret(),
                thirdPartyVO.getRedirectUri(), code);
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("client_id", thirdPartyVO.getAppKey());
        paramMap.put("client_secret", thirdPartyVO.getAppSecret());
        paramMap.put("grant_type", "authorization_code");
        paramMap.put("redirect_uri", thirdPartyVO.getRedirectUri());
        paramMap.put("code", code);
        try{
            String res = HttpClientUtil.doPost(tokenUrl, paramMap,"UTF-8");
            logger.info("get weibo access_token result:" + res);
            JSONObject resObj = JSONObject.parseObject(res);

            ThirdUserInfo thirdUserInfo = new ThirdUserInfo();
            thirdUserInfo.setAccessToken(resObj.getString("access_token"));
            thirdUserInfo.setExpiresIn(resObj.getString("expires_in"));
            thirdUserInfo.setOpenId(resObj.getString("uid"));
            return thirdUserInfo;
        }catch (Exception e) {
            logger.info("get weibo access_token occurs error " + e.getMessage());
        }
        return null;
    }

    @Override
    protected String getOpenId(ThirdUserInfo thirdUserInfo) {
        return null;
    }

    @Override
    protected Object getUserInfo(ThirdUserInfo thirdUserInfo) {
        String userinfo = String.format(thirdPartyVO.getUserInfoUrl(), thirdUserInfo.getAccessToken(), thirdUserInfo.getOpenId());
        try{
            String res = HttpClientUtil.doGet(userinfo,"UTF-8");
            logger.info("get weibo user info result:" + res);
            WeiBoUserInfoVO user = JSON.parseObject(res, WeiBoUserInfoVO.class);
            user.setOpenId(thirdUserInfo.getOpenId());
            return user;
        }catch (Exception e) {
            logger.info("get weibo user info occurs error " + e.getMessage());
        }
        return null;
    }

    @Override
    protected String biztype() {
        return ThirdParty.WB.getCode();
    }

    @Override
    protected ViewUser saveThirdUser(Object thirdUser) {
        ViewUser newWbUser = new ViewUser();
        if (thirdUser instanceof WeiBoUserInfoVO) {
            WeiBoUserInfoVO userInfoVO = (WeiBoUserInfoVO) thirdUser;
            ViewUser user = viewUserRepository.findViewUserByOpenId(userInfoVO.getOpenId());
            String nickname = userInfoVO.getScreen_name();
            String headpic = userInfoVO.getAvatar_large();
            if(user!=null){
                return updateUserInfo(user, nickname, headpic, viewUserRepository);
            }
            newWbUser = CommonUtil.build(CommonUtil.genereateUsername(userInfoVO.getOpenId(), ThirdParty.WB.getCode()), null,
                    ConstantUtil.defaultPassword(),null,headpic, userInfoVO.getOpenId(),nickname, ThirdParty.WB.getCode());
            viewUserRepository.save(newWbUser);
        }
        return newWbUser;
    }
}
