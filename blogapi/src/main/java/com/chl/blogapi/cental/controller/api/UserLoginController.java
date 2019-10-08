package com.chl.blogapi.cental.controller.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.util.StringUtils;

import com.chl.blogapi.cental.domain.ViewUser;
import com.chl.blogapi.cental.enumclass.BaseError;
import com.chl.blogapi.cental.service.ViewUserService;
import com.chl.blogapi.cental.vo.CommonResultVO;
import com.chl.blogapi.util.BlackListCenter;
import com.chl.blogapi.util.HttpUtil;
import com.chl.blogapi.util.Md5Util;
import com.chl.blogapi.util.SimpleRateLimiter;
import com.chl.blogapi.util.token.TokenUtil;

/**
 * @Auther: BoYanG
 * @Describe 用户登录
 */
@RestController
@RequestMapping(value = "/api")
public class UserLoginController {

    private static final Log logger = LogFactory.getLog(UserLoginController.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ViewUserService viewUserService;

    private SimpleRateLimiter userLoginLimiter = new SimpleRateLimiter("user_login_limiter", "ip_limit_30",60, 3600 * 3);

    @RequestMapping(value = "/login", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    public String login(
            @RequestParam("loginname") String loginname,
            @RequestParam("password") String password,
            @RequestParam("callback") String cb, HttpServletRequest requset){
        CommonResultVO result = new CommonResultVO();

        String ip = HttpUtil.getIpAddr(requset);

        //ip防刷
        if (!SimpleRateLimiter.isWhiteIp(ip)) {
            if (!userLoginLimiter.acquire(ip, redisTemplate)) {
                result.setErrorMessage(BaseError.IP_TOO_FREQUENT);
                return result.toGsonResultString();
            }
        }

        try {
            if(StringUtils.isEmpty(loginname)){
                return error(result, ErrTip.USERNAME_NULL, cb);
            }
            if(StringUtils.isEmpty(password)){
                return error(result, ErrTip.PASSWORD_NULL, cb);
            }
            //判断登录是否为邮箱
            Pattern rex = Pattern.compile("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$");
            Matcher matcher = rex.matcher(loginname);
            if (matcher.matches()) {
                loginname = viewUserService.getUserNameByEmail(loginname);
            }

            if(StringUtils.isEmpty(loginname)){
                return error(result, ErrTip.USERNAME_NOT_EXISTS, cb);
            }

            // 是否在黑名单
            if (!BlackListCenter.loginBlackCheck(loginname, redisTemplate)){
                return error(result, ErrTip.LOGIN_FAIL_MORE_TIMES, cb);
            }

            ViewUser viewUser = viewUserService.getViewUserByUserName(loginname);
            if (viewUser==null) {
                return error(result, ErrTip.USERNAME_NOT_EXISTS, cb);
            }

            if (viewUser.lock()) {
                return error(result, ErrTip.USERNAME_LOCK, cb);
            }

            if (!Md5Util.getMd5Password(password).equals(viewUser.getUserpsw())) {
                if (!BlackListCenter.loginFailTimeAddCheck(loginname, redisTemplate)) {
                    return error(result, ErrTip.LOGIN_FAIL_MORE_TIMES, cb);
                }
                return error(result, ErrTip.WRONG_PASSWORD, cb);
            }

            //登录成功
            result.put("token", TokenUtil.generateToken(loginname));
            result.put("userinfo", viewUser);
            // 清除失败次数
            BlackListCenter.loginFailTimeDelete(loginname, redisTemplate);
        } catch (Exception e) {
            logger.error("登录异常,登录账号为："+loginname, e);
            return error(result, ErrTip.PROGRAM_EXCEPTION, cb);
        }
        return result.toGsonResultString(cb);
    }

    private static String error(CommonResultVO result, ErrTip tip, String cb){
        result.setErrorMessage(tip.errorCode, tip.message);
        return result.toGsonResultString(cb);
    }

    enum ErrTip{

        SUCCESS("0", "success"),
        USERNAME_NULL("1", "用户名为空"),
        PASSWORD_NULL("2", "密码为空"),
        USERNAME_LOCK("3", "改账户已锁定"),
        USERNAME_NOT_EXISTS("4", "该用户名或邮箱不存在"),
        LOGIN_FAIL_MORE_TIMES("5", "该账号登录失败超过10次，请10分钟后再试"),
        WRONG_PASSWORD("6", "密码错误"),
        PROGRAM_EXCEPTION("7", "程序异常"),
        ;

        ErrTip(String errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
        }

        private final String errorCode;
        private final String message;

        public String getErrorCode() {
            return errorCode;
        }

        public String getMessage() {
            return message;
        }
    }
}
