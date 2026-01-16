package com.swiftRoute.aspect;

import com.swiftRoute.annotation.RedisCacheEvict;
import com.swiftRoute.annotation.RedisCachePut;
import com.swiftRoute.annotation.RedisCacheable;
import io.lettuce.core.dynamic.annotation.CommandNaming;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;

@Aspect
@CommandNaming
@AllArgsConstructor
public class RedisCacheAspect {

    private final RedisTemplate<String,Object> redisTemplate;

    @Around("@annotation(redisCacheable)")
    public Object cacheable(ProceedingJoinPoint joinPoint, RedisCacheable redisCacheable) throws Throwable {
        String key = redisCacheable.key();
        Object cached = redisTemplate.opsForValue().get(key);
        if(cached != null){
            return cached;
        }

        Object result = joinPoint.proceed();
        redisTemplate.opsForValue().set(
                key,
                result,
                redisCacheable.ttl(),
                redisCacheable.unit()
        );
        return result;
    }

    @Around("@annotation(redisCachePut)")
    public Object cachePut(ProceedingJoinPoint joinPoint, RedisCachePut redisCachePut) throws Throwable {
        Object result = joinPoint.proceed();
        redisTemplate.opsForValue().set(
                redisCachePut.key(),
                result,
                redisCachePut.ttl(),
                redisCachePut.unit()
        );
        return result;
    }

    @After("@annotation(redisCacheEvict)")
    public void cacheEvict(RedisCacheEvict redisCacheEvict){
        redisTemplate.delete(redisCacheEvict.key());
    }

}
