package com.hmdp.cacheUtils;

import java.lang.annotation.*;

/**
 * @author rth
 * @description: 标记该注解，会自动执行指定key的缓存删除动作
 * @date 2022/10/22
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CleanCache {

    // 前缀
    String prefix() default "";

    // redis key
    String field() default "";

}
