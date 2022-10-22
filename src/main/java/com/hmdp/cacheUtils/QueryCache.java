package com.hmdp.cacheUtils;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author rth
 * @description: redis缓存标识注解
 * @date 2022/10/20
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QueryCache {

    // 前缀
    String prefix() default "";

    // 过期时间，默认值是 1 分钟
    long expire() default 5;

    // 时间单位，默认是分钟
    TimeUnit timeUnit() default TimeUnit.MINUTES;

    // redis key
    String field() default "";

}
