package com.chl.blogapi.util.token;

import java.net.URLEncoder;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.chl.blogapi.util.RandomUtil;

/**
 * @Auther: BoYanG
 * @Describe 用户token信息
 */
public class TokenUtil {

    private static final Log logger = LogFactory.getLog(TokenUtil.class);

    private static Integer tokenExpireTime = 1209600000;
    private static Integer tokenRandomStringLen = 8;

    /**
     * token
     * @param username
     * @return
     * @throws Exception
     */
    public static String generateToken(String username) throws Exception {
        RSAPrivateKeyEncrypter rsaPrivateKeyEncrypter = new RSAPrivateKeyEncrypter();
        Date now = new Date();
        long expireTime = now.getTime() + tokenExpireTime;
        String token = rsaPrivateKeyEncrypter.encrypt(username + "&"
                + expireTime + "&"
                + RandomUtil.generate(tokenRandomStringLen));
        logger.info("token[v1.0]------>" + token + "------->username------->" + username);
        token = URLEncoder.encode(token, "UTF-8");
        return token;
    }

    /**
     * 校验token是否合法
     *
     * @param username
     * @param token
     * @return
     */
    public static boolean isTokenValid(String username, String token) {
        boolean flag = false;
        MutableObject<TokenInfo> tokenRef = new MutableObject<TokenInfo>();
        if (TokenVerifier.tryVerifyToken(token, tokenRef) && !StringUtils.isEmpty(username)) {
            // 解析token对象成功
            final TokenInfo tokenValue = tokenRef.getValue(); // 获取token对象
            logger.info("isExpired----->" + tokenValue.isExpired() + "------>username------>" + tokenValue.getUsername());
            if (!tokenValue.isExpired() && username.equals(tokenValue.getUsername())) {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * @param token
     * @description 根据token获取用户名，同时判断token是否过期，过期则返回空字符串
     */
    public static String getUsernameFromToken(String token) {
        RSAPrivateKeyEncrypter rsaPrivateKeyEncrypter = new RSAPrivateKeyEncrypter();
        String tokenDecode = rsaPrivateKeyEncrypter.decrypt(token);
        if (StringUtils.isEmpty(tokenDecode)) {
            return "";
        }

        String[] tokenArr = tokenDecode.split("&");
        if(tokenArr.length >= 3 && !"".equals(tokenArr[1])){
            long tokenExpireTimeStr = Long.parseLong(tokenArr[1]);
            if (tokenExpireTimeStr < (new Date()).getTime()) {
                return "";
            }
            return tokenArr[0];
        }
        return "";
    }
}
