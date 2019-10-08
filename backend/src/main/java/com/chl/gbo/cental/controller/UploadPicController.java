package com.chl.gbo.cental.controller;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.chl.gbo.cental.vo.CommonResultVO;
import com.chl.gbo.util.ConstantUtil;
import com.chl.gbo.util.HttpUtil;
import com.chl.gbo.util.redislock.DistributedLock;

/**
 * @Auther: BoYanG
 * @Describe 上传博文中的图片到nginx服务器
 */
@RestController
public class UploadPicController {

    @Autowired
    private DistributedLock distributedLock;

    private static final Log logger = LogFactory.getLog(UploadPicController.class);

    @PostMapping(value = "/upload/pic")
    public String upload(@RequestParam(value = "file", required = false) MultipartFile file,
                         HttpServletRequest requset){
        CommonResultVO commonResultVO = new CommonResultVO();
        try {
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
                    //上传图片
                    String picPath = ConstantUtil.getPicUploadPath() + "/";
                    String result = saveFile(file, picPath, upFileName, fileExt);
                    commonResultVO.put("path", result);
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

    private String saveFile(MultipartFile file, String uploadPath, String fileName, String fileExt) {
        try {
            File fileDirPath = new File(uploadPath);
            if (!fileDirPath.exists()) {
                logger.info("file path:" + uploadPath + " not exists, will mkdirs");
                if (fileDirPath.mkdirs()) {
                    logger.info("file path:" + uploadPath + " create successfully");
                } else {
                    logger.info("file path:" + uploadPath + " create failly");
                }
            }

            String newFile =fileName + fileExt;

            String filePath = uploadPath + newFile;
            // 转存文件
            file.transferTo(new File(filePath));

            logger.info("file path:" + filePath);

            // 返回域名响应路径
            return ConstantUtil.getImgHostName()+"/images/"+newFile;
        } catch (Exception e) {
            logger.error(e);
            return "";
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
        FILE_TOO_BIG("1", "file size is too big"),
        EXCEPTION("2", "upload file exception"),
        COVER_FIAL("3", "cover file fail"),
        UPLOADING("4", "uploading"),
        FILE_ULL("5", "file is null"),
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
