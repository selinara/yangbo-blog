package com.chl.blogapi.cental.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.chl.blogapi.cental.domain.ViewUser;
import com.chl.blogapi.cental.vo.ThirdPartyVO;

import java.util.List;

/**
 * @Auther: BoYanG
 * @Describe 三方登录服务类
 */
@Service
public class ThirdLoginSerivce {

    private ThirdPartyVO thirdPartyVO;

    @Autowired
    List<ThirdPartyOperation> thirdPartyOperations;

    /**
     * 获取三方授权路径
     * @param biz
     * @return
     */
    public String getAuthorizeUrl(String biz) {
        this.thirdPartyVO = new ThirdPartyVO(biz);
        return String.format(thirdPartyVO.getAuthorizeUrl(), thirdPartyVO.getAppKey(), thirdPartyVO.getRedirectUri());
    }

    /**
     * 用户注册后信息
     * @param code
     * @param biz
     * @return 循环遍历所有service，满足条件则调用
     */
    public ViewUser getThirdPartyUser(String code, String biz){
        for (ThirdPartyOperation thirdPartyOperation : thirdPartyOperations) {
            if (thirdPartyOperation.biztype().equals(biz)) {
                thirdPartyOperation.setThirdPartyVO(new ThirdPartyVO(biz));
                Object thirdUser = thirdPartyOperation.getThirdUser(code);
                if (thirdUser != null) {
                    return thirdPartyOperation.saveThirdUser(thirdUser);
                }
            }
        }
        return null;
    }
}
