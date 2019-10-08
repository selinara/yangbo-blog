package com.chl.blogapi.cental.controller.api;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.chl.blogapi.cental.enumclass.BaseError;
import com.chl.blogapi.cental.service.ViewUserService;
import com.chl.blogapi.cental.vo.CommonResultVO;
import com.chl.blogapi.util.ConstantUtil;
import com.chl.blogapi.util.HttpUtil;
import com.chl.blogapi.util.ImageUtil;
import com.chl.blogapi.util.SimpleRateLimiter;
import com.chl.blogapi.util.redislock.DistributedLock;
import com.chl.blogapi.util.token.TokenUtil;

/**
 * @Auther: BoYanG
 * @Describe 用户上传头像到nginx服务器,登录后的操作需要验证token
 */
@RestController
@RequestMapping(value = "/api")
public class UploadHeadPicController {

    @Autowired
    private ViewUserService viewUserService;

    @Autowired
    private DistributedLock distributedLock;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private SimpleRateLimiter uploadHeadLimiter = new SimpleRateLimiter("upload_head_limiter", "ip_limit_30",60, 3600 * 3);

    private static final Log logger = LogFactory.getLog(UploadHeadPicController.class);

    @RequestMapping(value = "/upload/pic")
    public String upload(@RequestParam(value = "file", required = false) MultipartFile file,
                         @RequestParam(value = "avatar_src", required = false) String avatar_src,
                         @RequestParam(value = "oldfile", required = false) String oldfile,
                         @RequestParam(value = "avatar_data", required = false) String avatar_data,
                         @RequestParam(value = "token", required = false) String token,
                         @RequestParam(value = "username", required = false) String username,
                         HttpServletRequest requset, HttpServletResponse response){

        //上传头像接口允许跨域
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "*");
        response.setHeader("Access-Control-Allow-Headers","x-requested-with,content-type");
        try {
            token = URLDecoder.decode(token, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        CommonResultVO commonResultVO = new CommonResultVO();

        String ip = HttpUtil.getIpAddr(requset);

        //ip防刷
        if (!SimpleRateLimiter.isWhiteIp(ip)) {
            if (!uploadHeadLimiter.acquire(ip, redisTemplate)) {
                commonResultVO.setErrorMessage(BaseError.IP_TOO_FREQUENT);
                return commonResultVO.toGsonResultString();
            }
        }

        try {
            if (!TokenUtil.isTokenValid(username, token)){
                commonResultVO.setErrorMessage(ErrorTip.TOKEN_INVALID.errorCode, ErrorTip.TOKEN_INVALID.message);
                return commonResultVO.toGsonResultString();
            }

            if (file == null) {
                commonResultVO.setErrorMessage(ErrorTip.FILE_ULL.errorCode, ErrorTip.FILE_ULL.message);
                return commonResultVO.toGsonResultString();
            }

            String originFileName = file.getOriginalFilename();

            String orignname = originFileName.substring(0, originFileName.lastIndexOf("."));

            String fileExt = originFileName.substring(originFileName.lastIndexOf(".")).toLowerCase();

            logger.info("file originFileName=" + originFileName + ", ip=" + HttpUtil.getIpAddr(requset));

            ErrorTip errorTip = checkFile(file);
            if (errorTip != null) {
                commonResultVO.setErrorMessage(errorTip.errorCode, errorTip.message);
                return commonResultVO.toGsonResultString();
            }

            // 加锁，不能重复操作
            String upFileName = orignname+'_'+System.currentTimeMillis();
            String lockKey = "file_" + upFileName;
            if(distributedLock.tryLock(lockKey, 5)){
                try {
                    //裁剪后新头像的图片路径
                    String picPath = ConstantUtil.getPicUploadPath() + "/";
                    String newFile =upFileName + fileExt;
                    String filePath = picPath + newFile;

                    //裁剪的图片坐标
                    JSONObject ava = JSONObject.parseObject(avatar_data);
                    Integer x = ava.getInteger("x");
                    Integer y = ava.getInteger("y");
                    Integer width = ava.getInteger("width");
                    Integer height = ava.getInteger("height");

                    //图片裁剪
                    ImageUtil.cutImage(file.getInputStream(), filePath, x, y, width, height);

                    String newPath = ConstantUtil.getImgHostName()+"/images/"+newFile;

                    viewUserService.updateUserHeadPic(username, newPath);

                    //清缓存
                    redisTemplate.delete("UserInfo:"+username+"_cache");

                    commonResultVO.put("result", newPath);

                    //上传成功，清除旧头像文件
                    String oldHeadPicName = oldfile.substring(oldfile.lastIndexOf("/")+1, oldfile.length());
                    String oldPath = picPath+oldHeadPicName;
                    File of = new File(oldPath);
                    if (of.exists() && of.isFile()) {
                        of.delete();
                    }
                    // success
                    return commonResultVO.toGsonResultString();
                } finally {
                    distributedLock.unLock(lockKey);
                }
            }
            logger.warn("the head pic is uploading, please don't do it concurrently:" + upFileName);
            commonResultVO.setErrorMessage(ErrorTip.UPLOADING.errorCode, ErrorTip.UPLOADING.message);
            return commonResultVO.toGsonResultString();
        } catch (Exception e) {
            logger.info("upload file occous error");
            commonResultVO.setErrorMessage(ErrorTip.EXCEPTION.errorCode, ErrorTip.EXCEPTION.message);
            return commonResultVO.toGsonResultString();
        }
    }

    private ErrorTip checkFile(MultipartFile file) {
        if (file.getSize() > ConstantUtil.getFileMaxSize()) {
            logger.info("the file is not allowed file");
            return ErrorTip.FILE_TOO_BIG;
        }
        return null;
    }

    enum ErrorTip {
        SUCCESS("0", "success"),
        FILE_TOO_BIG("1", "file too big"),
        EXCEPTION("2", "exception"),
        UPLOADING("3", "uploading"),
        FILE_ULL("4", "file is null"),
        TOKEN_INVALID("5", "token invalid"),
        ;
        private String errorCode;
        private String message;

        ErrorTip(String errorCode, String message) {
            setErrorCode(errorCode);
            setMessage(message);
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}
