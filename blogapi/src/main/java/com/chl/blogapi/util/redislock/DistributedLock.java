package com.chl.blogapi.util.redislock;


/**
 * Created by yuduy on 2017/9/27.
 */
public interface DistributedLock {

    Locker locker(String key, int lockTime);

    /**
     *
     * @param key
     * @param lockTime 锁住的时间 而非等待获取锁的时间 与jdk自带的锁相区分
     * @return
     */
    boolean tryLock(String key, int lockTime);

    boolean tryLock(String key, String value, int seconds);

    /**
     *
     * @param key
     * @param lockTime 锁住的时间 而非等待获取锁的时间 与jdk自带的锁相区分
     */
    void lock(String key, int lockTime);

    boolean unLock(String key);

//    boolean betterTryLock(String key, String value, int seconds);

    void betterUnLock(String key);

}
