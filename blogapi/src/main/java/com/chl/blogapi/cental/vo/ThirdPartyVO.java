package com.chl.blogapi.cental.vo;
import java.net.URLEncoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.chl.blogapi.cental.enumclass.ThirdParty;
import com.chl.blogapi.util.ConstantUtil;
import lombok.Data;

/**
 * @Auther: BoYanG
 * @Describe 三方登录相关参数
 */
@Data
public class ThirdPartyVO {

    private static final Log logger = LogFactory.getLog(ThirdPartyVO.class);

    private String biz;
    private String appKey;
    private String appSecret;
    private String redirectUri;
    private String authorizeUrl;
    private String accessTokenUrl;
    private String openIdUrl;
    private String userInfoUrl;

    public ThirdPartyVO(String biz) {
        this.biz = biz;
        this.appKey = ConstantUtil.appKey(biz);
        this.appSecret = ConstantUtil.appSecret(biz);
        this.redirectUri = getRedirectUri(biz);
        this.authorizeUrl = ConstantUtil.authorizeURL(biz);
        this.accessTokenUrl = ConstantUtil.accessTokenURL(biz);
        this.openIdUrl = ConstantUtil.openIdURL(biz);
        this.userInfoUrl = ConstantUtil.userInfoURL(biz);
    }

    private String getRedirectUri(String biz) {
        StringBuilder uri = new StringBuilder().append(ConstantUtil.getApiHostName());
        try {
            for (ThirdParty party: ThirdParty.values()) {
                if (party.getCode().equals(biz)) {
                    uri.append(party.getUri());
                    break;
                }
            }
            return URLEncoder.encode(uri.toString(), "utf-8");
        } catch (Exception e) {
            logger.error("third party redirect url encode occurs error", e);
        }
        return null;
    }
}
