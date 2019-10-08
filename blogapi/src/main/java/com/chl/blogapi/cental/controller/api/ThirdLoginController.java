package com.chl.blogapi.cental.controller.api;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.util.StringUtils;
import com.chl.blogapi.cental.domain.ViewUser;
import com.chl.blogapi.cental.enumclass.BaseError;
import com.chl.blogapi.cental.service.ThirdLoginSerivce;
import com.chl.blogapi.cental.service.ViewUserService;
import com.chl.blogapi.cental.vo.CommonResultVO;
import com.chl.blogapi.util.ConstantUtil;
import com.chl.blogapi.util.HttpUtil;
import com.chl.blogapi.util.SimpleRateLimiter;
import com.chl.blogapi.util.token.TokenUtil;

/**
 * @Auther: BoYanG
 * @Describe QQ三方登录
 */
@RestController
@RequestMapping(value = "/api")
public class ThirdLoginController {

    @Autowired
    private ThirdLoginSerivce thirdLoginSerivce;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private SimpleRateLimiter thirdLoginIpLimiter = new SimpleRateLimiter("third_login_ip", "ip_limit_30",60, 3600 * 3);

    /**
     * 返回三方登录授权页{qq,weibo}
     * @return
     */
    @RequestMapping(value = "/{biz}/login", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    public String loginto(@RequestParam("callback") String cb, @PathVariable("biz") String biztype,
                          @RequestParam(value = "qrid", required = false) String qrid,
                          @RequestParam(value = "appplt", required = false) String appplt,
                          HttpServletRequest request) {
        CommonResultVO result = new CommonResultVO();
        String ip = HttpUtil.getIpAddr(request);
        //ip防刷
        if (!SimpleRateLimiter.isWhiteIp(ip)) {
            if (!thirdLoginIpLimiter.acquire(ip, stringRedisTemplate)) {
                result.setErrorMessage(BaseError.IP_TOO_FREQUENT);
                return result.toGsonResultString(cb);
            }
        }
        if (biztype.contains("ph")) {
            HttpSession session = request.getSession();
            session.setAttribute("appplt", appplt);
            session.setAttribute("qrid", qrid);
            biztype = biztype.substring(2, biztype.length());
        }
        String authorizeUrl = thirdLoginSerivce.getAuthorizeUrl(biztype);
        if(StringUtils.isEmpty(authorizeUrl)){
            result.setErrorMessage(ErrTip.UNKONOWN_CODE.errorCode, ErrTip.UNKONOWN_CODE.message);
            return result.toGsonResultString(cb);
        }
        result.put("authurl", authorizeUrl);
        return result.toGsonResultString(cb);
    }

    /**
     * 三方登录获取用户信息
     * @return
     */
    @RequestMapping(value = "/{biz}/{biz}Login", method = RequestMethod.GET)
    public String qqLogin(
            @RequestParam(value = "code", required = false) String code,
            @PathVariable("biz") String biztype,
            HttpServletRequest request, HttpServletResponse response) throws Exception{
        CommonResultVO result = new CommonResultVO();
        String ip = HttpUtil.getIpAddr(request);
        //ip防刷
        if (!SimpleRateLimiter.isWhiteIp(ip)) {
            if (!thirdLoginIpLimiter.acquire(ip, stringRedisTemplate)) {
                result.setErrorMessage(BaseError.IP_TOO_FREQUENT);
                return result.toGsonResultString();
            }
        }
        String redirectHtml = "/index.html";
        HttpSession session = request.getSession();
        String qrid = (String) session.getAttribute("qrid");
        String appplt = (String) session.getAttribute("appplt");
        if(!StringUtils.isEmpty(qrid) && !StringUtils.isEmpty(appplt)){
            redirectHtml = "/ph-success.html?qrid=" +qrid+ "&appplt=" +appplt;
        }
        ViewUser viewUser = thirdLoginSerivce.getThirdPartyUser(code, biztype);
        if (null == viewUser) {
            result.setErrorMessage(ErrTip.GET_USERINFO_ERORR.getErrorCode(), ErrTip.GET_USERINFO_ERORR.getMessage());
            return result.toGsonResultString();
        }
        //三方登录直接显示登录成功
        response = addCookies(response, viewUser);
        response.sendRedirect(ConstantUtil.getHostName()+ redirectHtml);
        return null;
    }

    /**
     * 三方登录添加cookie
     * @param response
     * @param viewUser
     * @return
     * @throws Exception
     */
    private static HttpServletResponse addCookies(HttpServletResponse response, ViewUser viewUser)  throws Exception{
        response.addCookie(getCookie("LoginName", viewUser.getUsername()));
        response.addCookie(getCookie("headpic", viewUser.getHeadpic()));
        response.addCookie(getCookie("nickname", viewUser.getNickname()));
        response.addCookie(getCookie("token", TokenUtil.generateToken(viewUser.getUsername())));
        response.addCookie(getCookie("userId", String.valueOf(viewUser.getUserId())));
        return response;
    }

    private static Cookie getCookie(String key, String value){
        Cookie cookie = new Cookie(key, value);
        cookie.setPath("/");
        cookie.setMaxAge(3600*24*7);
        cookie.setDomain("yangshuqian.com");
        return cookie;
    }

    enum ErrTip{
        SUCCESS("0", "success"),
        PROGRAM_EXCEPTION("1", "program exception"),
        UNAUTHORIZED("2", "Unauthorized"),
        GET_OPENID_ERORR("3", "get openId occurs error"),
        GET_USERINFO_ERORR("4", "get userinfo occurs error"),
        UNKONOWN_CODE("5", "unknown code"),
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
