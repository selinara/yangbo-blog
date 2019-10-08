package com.chl.blogapi.cental.controller.api;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.chl.blogapi.cental.bean.MultiQrcode;
import com.chl.blogapi.cental.domain.ViewUser;
import com.chl.blogapi.cental.enumclass.BaseError;
import com.chl.blogapi.cental.service.ViewUserService;
import com.chl.blogapi.cental.vo.CommonResultVO;
import com.chl.blogapi.util.BlackListCenter;
import com.chl.blogapi.util.ConstantUtil;
import com.chl.blogapi.util.HttpUtil;
import com.chl.blogapi.util.MatrixToImageWriter;
import com.chl.blogapi.util.SimpleRateLimiter;
import com.chl.blogapi.util.token.TokenUtil;


/**
 * @Auther: BoYanG
 * @Describe 手机端扫web端二维码实现登录
 */
@RestController
@RequestMapping(value = "/api/qrcode")
public class QrcodeLoginController {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ViewUserService viewUserService;

    private static final Log logger = LogFactory.getLog(QrcodeLoginController.class);

    private SimpleRateLimiter qridUpdateIp = new SimpleRateLimiter("qrid_ip", "ip_limit_60",60, 3600 * 3);

    @RequestMapping(value = "/getQrid", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    public String qrid(@RequestParam(value = "appplt" ,required = false) String appplt,
                       @RequestParam(value = "callback",required = false) String cb,HttpServletRequest request){
        CommonResultVO result = new CommonResultVO();
        String remoteIp = HttpUtil.getIpAddr(request);
        //ip防刷
        if (!SimpleRateLimiter.isWhiteIp(remoteIp)) {
            if (!qridUpdateIp.acquire(remoteIp, stringRedisTemplate)) {
                result.setErrorMessage(BaseError.IP_TOO_FREQUENT);
                return result.toGsonResultString(cb);
            }
        }
        if (StringUtils.isEmpty(appplt)) {
            result.setErrorMessage("1", "设备号未传");
            return result.toGsonResultString(cb);
        }
        String uuid = UUID.randomUUID().toString();
        String qrid = uuid.replaceAll("-", "");
        logger.info("[appplt=" + appplt + "][qrid=" + qrid + "][ip=" + remoteIp + "]");
        MultiQrcode multiQrcode = new MultiQrcode();
        multiQrcode.setAppplt(appplt);
        multiQrcode.setQrid(qrid);
        multiQrcode.setStatus(MultiQrcode.QRID_STATUS_INIT);
        multiQrcode.setCreateTime(new Date());
        multiQrcode.setUpdateTime(new Date());
        String multiQrcodeRedisKey = "multiQrid:" + qrid;
        int qridExpireTime = ConstantUtil.getQridExpireTime();
        redisTemplate.opsForValue().set(multiQrcodeRedisKey, multiQrcode, qridExpireTime, TimeUnit.MINUTES);
        logger.info("appplt:"+appplt+"生成qrid，qrid:"+qrid);
        result.put("qrid", qrid);
        return result.toGsonResultString(cb);
    }

    @RequestMapping(value = "/getQrcode", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    public String qrcode(
            @RequestParam(value = "qrid" ,required = false) String qrid,
            @RequestParam(value = "size" ,required = false) Integer size,
            @RequestParam(value = "callback",required = false) String cb, HttpServletRequest request, HttpServletResponse response){
        CommonResultVO result = new CommonResultVO();
        String remoteIp = HttpUtil.getIpAddr(request);
        //ip防刷
        if (!SimpleRateLimiter.isWhiteIp(remoteIp)) {
            if (!qridUpdateIp.acquire(remoteIp, stringRedisTemplate)) {
                result.setErrorMessage(BaseError.IP_TOO_FREQUENT);
                return result.toGsonResultString(cb);
            }
        }
        if(StringUtils.isEmpty(qrid)){
            result.setErrorMessage("1", "qrid is null");
            return result.toGsonResultString(cb);
        }

        try {
            String multiQrcodeRedisKey = "multiQrid:" + qrid;
            MultiQrcode multiQrcode = (MultiQrcode) redisTemplate.opsForValue().get(multiQrcodeRedisKey);
            if (multiQrcode==null) {
                result.setErrorMessage("2", "二维码不存在");
                return result.toGsonResultString(cb);
            }
            //判断二维码是否已经过期
            if (isExpire(multiQrcode)) {
                result.setErrorMessage("3", "二维码过期");
                return result.toGsonResultString(cb);
            }
            response.setContentType("image/gif");
            size = size==null?MultiQrcode.DEFAULT_SIZE:size;
            StringBuilder sb = new StringBuilder(ConstantUtil.getHostName());
            sb.append("/waplogin.html?qrid="+multiQrcode.getQrid()).append("&appplt="+multiQrcode.getAppplt());
            MatrixToImageWriter.WriteImageStream(sb.toString(), size, size, response);
            //二维码状态不可逆
            if(multiQrcode.getStatus()<MultiQrcode.QRID_STATUS_SCAN){
                multiQrcode.setStatus(MultiQrcode.QRID_STATUS_SCAN);
                int expireTime = ConstantUtil.getQridExpireTime();
                redisTemplate.opsForValue().set(multiQrcodeRedisKey, multiQrcode, expireTime, TimeUnit.MINUTES);
            }
            return null;
        } catch (IOException e) {
            result.setErrorMessage("4", "程序异常");
            return result.toGsonResultString(cb);
        }
    }

    @RequestMapping(value = "/polling", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    public String polling(
            @RequestParam(value = "qrid", required = false) String qrid,
            @RequestParam(value = "appplt", required = false) String appplt,
            @RequestParam(value = "callback", required = false) String cb,
            HttpServletRequest request)
            throws Exception {
        CommonResultVO result = new CommonResultVO();
        try {
            if (StringUtils.isEmpty(qrid)) {
                result.setErrorMessage("1", "qrid is null");
                return result.toGsonResultString(cb);
            }

            if (StringUtils.isEmpty(appplt)) {
                result.setErrorMessage("2", "appplt is null");
                return result.toGsonResultString(cb);
            }

            String multiQrcodeRedisKey = "multiQrid:" + qrid;
            MultiQrcode multiQrcode = (MultiQrcode) redisTemplate.opsForValue().get(multiQrcodeRedisKey);

            if (multiQrcode==null) {
                result.setErrorMessage("3", "二维码不存在");
                return result.toGsonResultString(cb);
            }
            //判断二维码是否已经过期
            if (isExpire(multiQrcode)) {
                result.setErrorMessage("4", "二维码过期");
                return result.toGsonResultString(cb);
            }

            //把计数和平台数据放到redis，这里的统计逻辑应该和登录接口统计逻辑一致
            if (multiQrcode.getUsername() != null) {
                String username = URLDecoder.decode(multiQrcode.getUsername(), "UTF-8");
                if (username != null) {
                    boolean isInBlackList = BlackListCenter.loginBlackCheck(username, stringRedisTemplate);
                    if (!isInBlackList) {
                        logger.info("polling,account number failed more than limit,qrid:" + qrid);
                        result.setErrorMessage("5", "请稍后再试");
                        return result.toGsonResultString(cb);
                    }
                }
            }
            result.put("status", multiQrcode.getStatus());
            //已登录状态返回用户的username+token信息;已支付状态只返回状态码
            if (multiQrcode.QRID_STATUS_SUCCESS_GRANT == multiQrcode.getStatus()) {
                result.put("token", multiQrcode.getToken());
                ViewUser viewUser = viewUserService.getViewUserByUserName(URLDecoder.decode(multiQrcode.getUsername(), "utf-8"));
                result.put("userinfo", viewUser);
            }
            logger.info("appplt:"+appplt+"正尝试扫描，当前状态为：" + multiQrcode.getStatus() + " ,username: " + multiQrcode.getUsername());
            return result.toGsonResultString(cb);
        } catch (Exception e) {
            result.setErrorMessage("10", "程序异常");
            return result.toGsonResultString(cb);
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET, produces="text/html;charset=UTF-8")
    @ResponseBody
    public String getMultiQrid(
            @RequestParam(value = "qrid", required = false) String qrid,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "token", required = false) String token,
            @RequestParam(value = "appplt", required = false) String appplt,
            @RequestParam(value = "callback", required = false) String cb)
            throws Exception {
        CommonResultVO result = new CommonResultVO();
        try {
            if (StringUtils.isEmpty(qrid)) {
                result.setErrorMessage("1", "qrid is null");
                return result.toGsonResultString(cb);
            }

            if (StringUtils.isEmpty(appplt)) {
                result.setErrorMessage("2", "appplt is null");
                return result.toGsonResultString(cb);
            }

            if (StringUtils.isEmpty(status)) {
                result.setErrorMessage("3", "status is null");
                return result.toGsonResultString(cb);
            }

            if (!"0".equals(status)) {
                logger.info("login,status is error,status: " + status);
                result.setErrorMessage("4", "status is error");
                return result.toGsonResultString(cb);
            }


            if (StringUtils.isEmpty(username)) {
                logger.info("login,username is null,username: " + username);
                result.setErrorMessage("5", "username is null");
                return result.toGsonResultString(cb);
            }

            ViewUser viewUser = viewUserService.getViewUserByUserName(username);
            if (viewUser == null) {
                logger.info("login,username does not exist,username: " + username);
                result.setErrorMessage("6", "username does not exist");
                return result.toGsonResultString(cb);
            }

            if (StringUtils.isEmpty(token)) {
                logger.info("login,token is null,username: " + username);
                result.setErrorMessage("7", "token is null");
                return result.toGsonResultString(cb);
            }

            if (!TokenUtil.isTokenValid(username, token)) {
                logger.info("login,token is error,username: " + username);
                result.setErrorMessage(BaseError.TOKEN_VOERDUE);
                return result.toGsonResultString(cb);
            }

            //可增加用户锁定逻辑，本博客没有此逻辑
//            if(User.USER_STATE_LOCKED.equals(user.getUserState())) {
//                logger.info("the user status has been locked.: " + username);
//                commonResultVO.setError(Error.PARAM_USERSTATUS_LOCKED);
//                return GsonGenerateResultUtil.generateResult(commonResultVO, format, cb);
//            }
            boolean isInBlackList = BlackListCenter.loginBlackCheck(username, stringRedisTemplate);
            if (!isInBlackList) {
                logger.info("polling,account number failed more than limit,qrid:" + qrid);
                result.setErrorMessage("8", "请稍后再试");
                return result.toGsonResultString(cb);
            }
            String multiQrcodeRedisKey = "multiQrid:" + qrid;
            MultiQrcode multiQrcode = (MultiQrcode) redisTemplate.opsForValue().get(multiQrcodeRedisKey);
            if (multiQrcode==null) {
                result.setErrorMessage("9", "二维码不存在");
                return result.toGsonResultString(cb);
            }

            //判断二维码是否已经过期
            if (isExpire(multiQrcode)) {
                result.setErrorMessage("10", "二维码过期");
                return result.toGsonResultString(cb);
            }
            //不允许逆向操作
            if (multiQrcode.QRID_STATUS_SUCCESS_GRANT <= multiQrcode.getStatus()) {
                logger.info("qrid has completed the operation,qrid:" + qrid);
                result.setErrorMessage("11", "不允许逆向操作");
                return result.toGsonResultString(cb);
            }

            //生成新版token给生成二维码端使用
            String multiQrcodeToken = TokenUtil.generateToken(username);

            multiQrcode.setUsername(URLEncoder.encode(username, "UTF-8"));
            multiQrcode.setToken(multiQrcodeToken);
            multiQrcode.setStatus(MultiQrcode.QRID_STATUS_SUCCESS_GRANT);
            int expireTime = ConstantUtil.getQridExpireTime();
            redisTemplate.opsForValue().set(multiQrcodeRedisKey, multiQrcode, expireTime, TimeUnit.MINUTES);
            logger.info("[username=" + username + "][appplt=" + multiQrcode.getAppplt() + "]");
            return result.toGsonResultString(cb);
        } catch (Exception e) {
            logger.info("程序异常", e);
            result.setErrorMessage("12", "程序异常");
            return result.toGsonResultString(cb);
        }
    }

    //判断二维码是否过期
    private boolean isExpire(MultiQrcode multiQrcode) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(multiQrcode.getCreateTime());
        cal.add(Calendar.MINUTE, ConstantUtil.getQridExpireTime());
        Date nowDate = new Date();
        if (nowDate.after(cal.getTime())) {
            return true;
        }
        return false;
    }

    public byte[] toByteArray (Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray ();
            oos.close();
            bos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bytes;
    }

}
