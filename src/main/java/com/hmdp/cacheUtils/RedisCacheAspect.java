package com.hmdp.cacheUtils;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hmdp.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author rth
 * @description: redis缓存切面类
 * @date 2022/10/20
 */
@Aspect
@Component
@Slf4j
public class RedisCacheAspect {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Pointcut("@annotation(com.hmdp.cacheUtils.QueryCache)")
    public void pt() {}

    @Around("pt()")
    public Result around(ProceedingJoinPoint point) {
        try {
            // 获取方法签名
            MethodSignature signature = (MethodSignature)point.getSignature();
            Object[] args = point.getArgs();

            // 通过签名获取注解
            QueryCache annotation = signature.getMethod().getAnnotation(QueryCache.class);
            // 根据注解获取前缀、标识key、过期时间等信息
            String prefix = annotation.prefix();
            String field = annotation.field();
            String key = CacheUtils.parseKey(prefix, field, point);

            // 从缓存中读取数据
            String cacheData = stringRedisTemplate.opsForValue().get(key);
            // 命中，直接返回
            if (StringUtils.isNotBlank(cacheData)) {
                return Result.ok(JSONUtil.parse(cacheData));
            }
            // 未命中，查询数据库，即执行方法原有逻辑
            Result result = (Result) point.proceed(args);
            if (result.getSuccess()) {
                // 查询出结果，将数据写入缓存
                stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(result.getData()));
                return result;
            }
            return result;

        } catch (Throwable throwable) {
            log.error(throwable.getMessage());
            throwable.printStackTrace();
        }
        return Result.fail("查询结果为空！");
    }

}
