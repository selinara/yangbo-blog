package com.chl.gbo.util;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @Auther: BoYanG
 * @Describe 用户登录账号密码错误超过10，则进入黑名单15分钟
 */
public class BlackListCenter {
    //10次限制
    private static final int PASS_WRONG_LOCK_TIME = 10;
    //用户名密码错误检测间隔
    private static final int PASSWORD_CHECK_SECTION_MILLIONS = 60 * 10;
    //失败超过限制锁定时长
    private static final int PASSWORD_BLACK_EXPIRE_MILLIONS = 60 * 10;

    /**
     * 检验用户是否在黑名单
     * @param username
     * @param redisTemplate
     * @return
     */
    public static boolean loginBlackCheck(String username, StringRedisTemplate redisTemplate){
        String passBlackKey = new StringBuilder().append("PassWrongBlack:").append(username).toString();
        String passBlackValue = redisTemplate.opsForValue().get(passBlackKey);
        if(StringUtils.isNotEmpty(passBlackValue)){
            return false;
        }
        return true;
    }

    /**
     * 用户名密码错误时缓存的次数+1，如果达到次数限制，加入黑名单2小时，删除失败次数记录
     * @param username
     */
    public static boolean loginFailTimeAddCheck(String username, StringRedisTemplate redisTemplate){
        String passBlackKey = new StringBuilder().append("PassWrongBlack:").append(username).toString();
        String passWrongKey = new StringBuilder().append("PassWrongTimes:").append(username).toString();

        if(StringUtils.isEmpty(redisTemplate.opsForValue().get(passWrongKey))){
            redisTemplate.opsForValue().set(passWrongKey, String.valueOf(System.currentTimeMillis()) + "&" + 1, PASSWORD_CHECK_SECTION_MILLIONS, TimeUnit.SECONDS);
        }else{
            String tValue = redisTemplate.opsForValue().get(passWrongKey);
            String[] tValues = tValue.split("&");
            if(tValues.length == 2){
                if(System.currentTimeMillis() - Long.valueOf(tValues[0]) < PASSWORD_CHECK_SECTION_MILLIONS * 1000){//统一单位为毫秒
                    if(Integer.valueOf(tValues[1]) + 1 <= PASS_WRONG_LOCK_TIME){
                        redisTemplate.opsForValue().set(passWrongKey, String.valueOf(System.currentTimeMillis()) + "&" + (Integer.valueOf(tValues[1]) + 1),
                                PASSWORD_CHECK_SECTION_MILLIONS, TimeUnit.SECONDS);
                    }else{
                        redisTemplate.opsForValue().set(passBlackKey, "true", PASSWORD_BLACK_EXPIRE_MILLIONS, TimeUnit.SECONDS);
                        redisTemplate.delete(passWrongKey);
                        return false;
                    }
                }else{
                    redisTemplate.opsForValue().set(passWrongKey, String.valueOf(System.currentTimeMillis()) + "&" + 1, PASSWORD_CHECK_SECTION_MILLIONS, TimeUnit.SECONDS);
                }
            }
        }
        return true;
    }

    /**
     * 未超过限制情况下，登陆成功清除失败记录缓存
     * @param username
     */
    public static void loginFailTimeDelete(String username, StringRedisTemplate redisTemplate){
        String passWrongKey = new StringBuilder().append("PassWrongTimes:").append(username).toString();
        redisTemplate.delete(passWrongKey);
    }
}
