package com.chl.gbo.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @Auther: BoYanG
 */
public class BCryptUtil {

    private static BCryptPasswordEncoder crypt = new BCryptPasswordEncoder(4);

    public static String encode(String password){
        return crypt.encode(password);
    }

}
