package com.hmdp.cacheUtils;

import com.hmdp.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * @author rth
 * @description: 删除缓存切面处理逻辑
 * @date 2022/10/22
 */
@Aspect
@Component
@Slf4j
public class CleanCacheAspect {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Pointcut("@annotation(com.hmdp.cacheUtils.CleanCache)")
    public void pt() {}

    /**
     *  切面处理逻辑
     * @param point
     * @return
     */
    @Around("pt()")
    public Result around(ProceedingJoinPoint point) {
        try {
            // 0. 执行数据库修改逻辑（原有逻辑）
            Result result = (Result) point.proceed(point.getArgs());
            if (! result.getSuccess()) {
                return result;
            }

            // 1. 获取@CleanCache注解相关信息
            // 2. 根据注解信息拼接生成需要删除的缓存key
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            CleanCache annotation = method.getAnnotation(CleanCache.class);
            String cacheKey = CacheUtils.parseKey(annotation.prefix(), annotation.field(), point);
            log.info("删除key：{}", cacheKey);
            // 3. 删除缓存
            stringRedisTemplate.delete(cacheKey);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return Result.ok();
    }

}
