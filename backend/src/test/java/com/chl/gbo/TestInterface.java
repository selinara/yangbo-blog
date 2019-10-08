package com.chl.gbo;

import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @Auther: BoYanG
 * @Describe TODO
 */
public class TestInterface {

    @Test
    public void testEncode(){
        String password = "123456";
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(4);
        String enPassword = bCryptPasswordEncoder.encode(password);
        System.out.println(enPassword);
    }

}
