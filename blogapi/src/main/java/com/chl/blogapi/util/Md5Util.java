package com.chl.blogapi.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.Assert;

public class Md5Util {

    public static String getMd5Password(String password) {
        Assert.notNull(password, "The password must not be null");
        String md5PassWord = DigestUtils.md5Hex(password).substring(8, 24).toLowerCase();
        return md5PassWord;
    }

}
