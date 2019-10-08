package com.chl.gbo.util.redislock;

/**
 * Created by yuduy on 2017/9/27.
 */
public interface Locker {

    boolean tryLock();

    void lock();

    boolean unLock();

}
