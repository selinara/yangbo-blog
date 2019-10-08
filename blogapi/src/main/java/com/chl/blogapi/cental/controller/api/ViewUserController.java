package com.chl.blogapi.cental.controller.api;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chl.blogapi.cental.bean.MailDO;
import com.chl.blogapi.cental.enumclass.BaseError;
import com.chl.blogapi.cental.exception.BlogException;
import com.chl.blogapi.cental.service.MailService;
import com.chl.blogapi.cental.service.ViewUserService;
import com.chl.blogapi.cental.threadpool.SynaOperationUtil;
import com.chl.blogapi.cental.vo.CommonResultVO;
import com.chl.blogapi.util.AntiXssUtil;
import com.chl.blogapi.util.HttpUtil;
import com.chl.blogapi.util.Md5Util;
import com.chl.blogapi.util.RedisLikeUtil;
import com.chl.blogapi.util.SimpleRateLimiter;
import com.chl.blogapi.util.token.TokenUtil;

/**
 * @Auther: BoYanG
 * @Describe 邮箱注册-用户信息更改操作
 */
@RestController
@RequestMapping(value = "/api")
public class ViewUserController {

    private static final Log logger = LogFactory.getLog(ViewUserController.class);

    @Autowired
    private MailService mailService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ViewUserService viewUserService;

    private static final long MAIL_EXPIRE_TIME = 900; //验证码过期时间为900s
    private static final long DO_NOT_REPEAT_TIME = 60; //60s不可重复发邮件

    private SimpleRateLimiter mailSendLimiter = new SimpleRateLimiter("mail_send_ip", "ip_limit_60",60, 3600 * 3);

    //-----------------------------------------用户注册操作--------------------------------------------------------------

    @RequestMapping(value = "/mail/check/username", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    public String checkUsername(
            @RequestParam("username") String username,
            @RequestParam("callback") String cb){
        CommonResultVO result = new CommonResultVO();
        String usernames = redisTemplate.opsForValue().get("Collection:username");
        String str = ","+username+",";
        if (usernames.indexOf(str) != -1) {
            result.setErrorMessage("1","username is existed");
            return result.toGsonResultString(cb);
        }
        return result.toGsonResultString(cb);
    }

    @RequestMapping(value = "/mail/check/usermail", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    public String checkUsermail(
            @RequestParam("usermail") String usermail,
            @RequestParam("callback") String cb){
        CommonResultVO result = new CommonResultVO();
        String usernames = redisTemplate.opsForValue().get("Collection:usermail");
        String str = ","+usermail+",";
        if (usernames.indexOf(str) != -1) {
            result.setErrorMessage("1","usermail is existed");
            return result.toGsonResultString(cb);
        }
        return result.toGsonResultString(cb);
    }

    @RequestMapping(value = "/mail/send", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    public String sendmail(
            @RequestParam("usermail") String usermail,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam("callback") String cb, HttpServletRequest requset){

        CommonResultVO result = new CommonResultVO();

        String ip = HttpUtil.getIpAddr(requset);

        //ip防刷
        if (!SimpleRateLimiter.isWhiteIp(ip)) {
            if (!mailSendLimiter.acquire(ip, redisTemplate)) {
                result.setErrorMessage(BaseError.IP_TOO_FREQUENT);
                return result.toGsonResultString(cb);
            }
        }

        try {
            logger.info("邮箱："+usermail+"，正在尝试发送邮件");
            String mailKey = RedisLikeUtil.mailKey(usermail);
            String keylimit60 = RedisLikeUtil.keyLimit60(mailKey);
            // 60s内不可重复发送
            if(StringUtils.isNotEmpty(redisMailKeyVal(keylimit60))){
                result.setErrorMessage(ErrTip.MAIL_HAS_SEND.errorCode, ErrTip.MAIL_HAS_SEND.message);
                return result.toGsonResultString(cb);
            }

            //随机生成验证码
            String code = StringUtils.isEmpty(redisMailKeyVal(mailKey))? RedisLikeUtil.randomCheckcode():redisMailKeyVal(mailKey);

            //发送验证码
            if(!mailService.sendTextMail(new MailDO(code,usermail, type))){
                result.setErrorMessage(ErrTip.MAIL_SEND_FAIL.errorCode, ErrTip.MAIL_SEND_FAIL.message);
                return result.toGsonResultString(cb);
            }

            //发送成功
            redisTemplate.opsForValue().set(mailKey, code, MAIL_EXPIRE_TIME, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(keylimit60, "1", DO_NOT_REPEAT_TIME, TimeUnit.SECONDS);

            logger.info("邮箱："+usermail+"，注册邮件发送成功");

        } catch (Exception e) {
            logger.info("邮箱："+usermail+"，注册邮件发送异常");
            result.setErrorMessage(ErrTip.PROGRAM_EXCEPTION.errorCode, ErrTip.PROGRAM_EXCEPTION.message);
            return result.toGsonResultString(cb);
        }
        return result.toGsonResultString(cb);
    }

    @RequestMapping(value = "/mail/register", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    public String checkcode(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "usermail",required = false) String usermail,
            @RequestParam(value = "code",required = false) String checkcode,
            @RequestParam(value ="password",required = false) String password,
            @RequestParam(value ="callback",required = false) String cb, HttpServletRequest request){
        CommonResultVO result = new CommonResultVO();

        String ip = HttpUtil.getIpAddr(request);
        //ip防刷
        if (!SimpleRateLimiter.isWhiteIp(ip)) {
            if (!mailSendLimiter.acquire(ip, redisTemplate)) {
                result.setErrorMessage(BaseError.IP_TOO_FREQUENT);
                return result.toGsonResultString(cb);
            }
        }

        try {
            logger.info("邮箱："+usermail+"，正在注册");

            String code = redisMailKeyVal(RedisLikeUtil.mailKey(usermail));
            if (StringUtils.isEmpty(code) || !checkcode.equals(code)) {
                result.setErrorMessage(ErrTip.CODE_VALIDATE_FIAL.errorCode, ErrTip.CODE_VALIDATE_FIAL.message);
                return result.toGsonResultString(cb);
            }
            if(!viewUserService.saveNewMailUser(username, usermail, password, HttpUtil.getIpAddr(request))){
                throw new BlogException("register error");
            }
            // 更新用户名集合缓存
            SynaOperationUtil.updateUserNames(redisTemplate, viewUserService,username);
            SynaOperationUtil.updateUserMails(redisTemplate, viewUserService);
        } catch (Exception e) {
            logger.info("邮箱："+usermail+"，注册异常");
            result.setErrorMessage(ErrTip.PROGRAM_EXCEPTION.errorCode, ErrTip.PROGRAM_EXCEPTION.message);
            return result.toGsonResultString(cb);
        }
        logger.info("邮箱："+usermail+"，注册成功");
        return result.toGsonResultString(cb);
    }

    @RequestMapping(value = "/mail/changepsw", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    public String changepsw(
            @RequestParam(value = "usermail",required = false) String usermail,
            @RequestParam(value = "code",required = false) String checkcode,
            @RequestParam(value ="password",required = false) String password,
            @RequestParam(value ="callback",required = false) String cb, HttpServletRequest request){
        CommonResultVO result = new CommonResultVO();

        String ip = HttpUtil.getIpAddr(request);
        //ip防刷
        if (!SimpleRateLimiter.isWhiteIp(ip)) {
            if (!mailSendLimiter.acquire(ip, redisTemplate)) {
                result.setErrorMessage(BaseError.IP_TOO_FREQUENT);
                return result.toGsonResultString(cb);
            }
        }

        try {
            logger.info("邮箱："+usermail+"，正在修改密码");

            String code = redisMailKeyVal(RedisLikeUtil.mailKey(usermail));
            if (StringUtils.isEmpty(code) || !checkcode.equals(code)) {
                result.setErrorMessage(ErrTip.CODE_VALIDATE_FIAL.errorCode, ErrTip.CODE_VALIDATE_FIAL.message);
                return result.toGsonResultString(cb);
            }
            if(!viewUserService.changePsw(usermail, Md5Util.getMd5Password(password))){
                throw new BlogException("change password error");
            }
        } catch (Exception e) {
            logger.info("邮箱："+usermail+"，修改密码异常");
            result.setErrorMessage(ErrTip.PROGRAM_EXCEPTION.errorCode, ErrTip.PROGRAM_EXCEPTION.message);
            return result.toGsonResultString(cb);
        }
        logger.info("邮箱："+usermail+"，修改密码成功");
        // 清除用户缓存
        SynaOperationUtil.cleanUserCache(redisTemplate, viewUserService.getUserNameByEmail(usermail));

        return result.toGsonResultString(cb);
    }

    private String redisMailKeyVal(String key){
        return redisTemplate.opsForValue().get(key);
    }

    //------------------------------------------------用户登录后的操作---------------------------------------------
    @RequestMapping(value = "/update/user/info", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    public String updateUserInfo(
            @RequestParam(value ="username", required = false) String username,
            @RequestParam(value ="nickname", required = false) String nickname,
            @RequestParam(value ="phone", required = false) String phone,
            @RequestParam(value ="token", required = false) String token,
            @RequestParam(value ="callback", required = false) String cb, HttpServletRequest request){

        CommonResultVO result = new CommonResultVO();

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(nickname) || StringUtils.isEmpty(token)) {
            result.setErrorMessage(ErrTip.NULL_EXIST.errorCode, ErrTip.NULL_EXIST.message);
            return result.toGsonResultString(cb);
        }

        if (!TokenUtil.isTokenValid(username, token)){
            result.setErrorMessage(ErrTip.TOKEN_INVALID.errorCode, ErrTip.TOKEN_INVALID.message);
            return result.toGsonResultString(cb);
        }

        //过滤特殊字符，防止xss攻击
        nickname = filterHtml(nickname);
        phone = filterHtml(phone);

        viewUserService.updateUserInfoByUserName(username, nickname, phone);

        // 清除用户缓存
        SynaOperationUtil.cleanUserCache(redisTemplate, username);

        return result.toGsonResultString(cb);
    }

    private static String filterHtml(String value){
        if (AntiXssUtil.isScriptParaValue(value)) {
            value = AntiXssUtil.HtmlEncode(value);
        }
        if (AntiXssUtil.isContainDangerChar4Url(value)) {
            value = AntiXssUtil.filterParameter4Url(value);
        }
        return value;
    }

    enum ErrTip{

        SUCCESS("0", "success"),
        MAIL_HAS_SEND("1", "usermail has sended in 60s"),
        MAIL_SEND_FAIL("2", "mail send fail"),
        PROGRAM_EXCEPTION("3", "program exception"),
        CODE_VALIDATE_FIAL("4", "code validate fail"),
        TOKEN_INVALID("5", "token invalid"),
        NULL_EXIST("6", "param is null"),
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
