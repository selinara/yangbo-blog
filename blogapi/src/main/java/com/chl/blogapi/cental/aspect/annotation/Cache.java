package com.chl.blogapi.cental.aspect.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {

    /***
     * redis中缓存前缀
     *
     * @return
     */
    String prefix();

    /***
     * 保存时间长度
     *
     * @return
     */
    long time() default 3;

    /**
     * 缓存时间单位,默认为秒
     * @return
     */
    TimeUnit unit() default TimeUnit.MINUTES;
}
