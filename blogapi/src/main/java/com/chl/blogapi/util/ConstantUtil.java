package com.chl.blogapi.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;

public class ConstantUtil{

    private static Log logger = LogFactory.getLog(ConstantUtil.class);

    private static ResourceBundle resourceBundle;

    private static Map<String, Integer> limiterMap = Maps.newHashMap();

    public static String getHostName(){
        return resourceBundle.getString("HOST_NAME");
    }

    public static String getImgHostName(){
        return resourceBundle.getString("IMG_HOST_NAME");
    }

    public static String getApiHostName(){
        return resourceBundle.getString("API_HOST_NAME");
    }

    public static String getPicUploadPath(){
        return resourceBundle.getString("PICTURE_UPLOAD_PATH");
    }

    static {
        resourceBundle = ResourceBundle.getBundle("constant");
    }

    public static Integer getFileMaxSize(){
        String max =  resourceBundle.getString("FILE_MAX_SIZE");
        return Integer.parseInt(max);
    }

    public static String getDefaultHeadPic(){
        return resourceBundle.getString("DEFAULT_HEADPIC");
    }


    /**
     *------------------------------------------- 三方登录------------------------------------------------
     */
    public static String appKey(String biz){
        return resourceBundle.getString(biz+"_appKey");
    }
    public static String appSecret(String biz){
        return resourceBundle.getString(biz+"_appSecret");
    }
    public static String authorizeURL(String biz){
        return resourceBundle.getString(biz+"_authorizeURL");
    }
    public static String accessTokenURL(String biz){
        return resourceBundle.getString(biz+"_accessTokenURL");
    }
    public static String openIdURL(String biz){
        return resourceBundle.getString(biz+"_openIdURL");
    }
    public static String userInfoURL(String biz){
        return resourceBundle.getString(biz+"_userInfoURL");
    }
    //-----------------------------------------------end-----------------------------------------------------

    public static String defaultPassword(){
        return resourceBundle.getString("default_password");
    }

    public static List<String> getIpWhiteList() {
        String ipWhiteStr = resourceBundle.getString("ipWhiteList");
        return Arrays.asList(ipWhiteStr.split(","));
    }

    public static int getLimiterByKey(String key){
        String limiterConfigs = resourceBundle.getString("limiterConfigs");
        try {
            JSONObject jObj = JSONObject.parseObject(limiterConfigs);
            if (jObj != null && jObj.get(key) != null) {
                return jObj.getInteger(key);
            }
            logger.info("无当前key的限制配置，key:" + key);
        } catch (Exception e) {
            logger.info("当前配置限制JSON格式出错，key:" + key);
        }
        return 150;
    }

    public static Integer getQridExpireTime(){
        return Integer.parseInt(resourceBundle.getString("QRID_EXPIRE_TIME"));
    }
}
