package com.swiftRoute.aspect;

import com.swiftRoute.annotation.RedisCacheEvict;
import com.swiftRoute.annotation.RedisCachePut;
import com.swiftRoute.annotation.RedisCacheable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;

@Aspect
@Component
@AllArgsConstructor
@Slf4j
public class RedisCacheAspect {

    private final RedisTemplate<String,Object> redisTemplate;
    private final ExpressionParser parser = new SpelExpressionParser();

    /*
    RedisCacheable annotation AOP Logic
     */
    @Around("@annotation(redisCacheable)")
    public Object cacheable(ProceedingJoinPoint joinPoint, RedisCacheable redisCacheable) throws Throwable {
        log.info("inside cacheable...");
        String key = parseKey(redisCacheable.key(), joinPoint);
        log.info("cacheable key : {}",key);
        Object cached = redisTemplate.opsForValue().get(key);
        if(cached != null){
            log.info("Cache HIT for key: {}", key);
            return convertIfNecessary(cached,joinPoint);
        }
        log.info("Cache MISS for key: {}", key);
        Object result = joinPoint.proceed();
        redisTemplate.opsForValue().set(
                key,
                result,
                redisCacheable.ttl(),
                redisCacheable.unit()
        );
        log.info("Cached result for key: {}", key);
        return result;
    }

    @Around("@annotation(redisCachePut)")
    public Object cachePut(ProceedingJoinPoint joinPoint, RedisCachePut redisCachePut) throws Throwable {
        String key = parseKey(redisCachePut.key(), joinPoint);
        log.info("cachePut key : {}", key);
        Object result = joinPoint.proceed();
        redisTemplate.opsForValue().set(
                key,
                result,
                redisCachePut.ttl(),
                redisCachePut.unit()
        );
        log.info("Updated cache for key: {}", key);
        return result;
    }

    @After("@annotation(redisCacheEvict)")
    public void cacheEvict(RedisCacheEvict redisCacheEvict, ProceedingJoinPoint joinPoint){
        String key = parseKey(redisCacheEvict.key(), joinPoint);
        redisTemplate.delete(key);
    }

    /**
     * Evaluates SpEL expression with method parameters.
     * Example: "'post:' + #id" with id=5 → "post:5"
     */
    private String parseKey(String keyExpression, ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // Create evaluation context
        StandardEvaluationContext context = new StandardEvaluationContext();

        // Get parameter names and values
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        // Register parameters in context
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        // Evaluate the expression
        try {
            Object value = parser.parseExpression(keyExpression).getValue(context);
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            log.error("Failed to parse key expression: {}", keyExpression, e);
            // Fallback: use method name + args
            return method.getName() + ":" + String.join("_",
                    java.util.Arrays.stream(args).map(String::valueOf).toArray(String[]::new));
        }
    }

    /*
    When Redis stores complex Java objects (like DTOs),
    they are often deserialized as LinkedHashMap instead of the original class.
     */
    private Object convertIfNecessary(Object cached, ProceedingJoinPoint joinPoint) {
        if (cached instanceof LinkedHashMap<?, ?>) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Class<?> returnType = signature.getReturnType();

            ObjectMapper mapper = new ObjectMapper();
            return mapper.convertValue(cached, returnType);
        }
        return cached;
    }
}
