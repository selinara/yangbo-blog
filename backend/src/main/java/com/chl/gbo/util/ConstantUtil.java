package com.chl.gbo.util;

import java.util.ResourceBundle;

public class ConstantUtil {

    private static ResourceBundle resourceBundle;

    static {
        resourceBundle = ResourceBundle.getBundle("constant");
    }

    public static String getHostName(){
        return resourceBundle.getString("HOST_NAME");
    }

    public static String getImgHostName(){
        return resourceBundle.getString("IMG_HOST_NAME");
    }

    public static String getPicUploadPath(){
        return resourceBundle.getString("PICTURE_UPLOAD_PATH");
    }

    public static Integer getFileMaxSize(){
        String max =  resourceBundle.getString("FILE_MAX_SIZE");
        return Integer.parseInt(max);
    }

    public static String getDefaultHeadPic(){
        return resourceBundle.getString("DEFAULT_HEADPIC");
    }

}
