package com.swiftRoute.service;

import com.swiftRoute.util.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * RedisService: Service for Redis Operations
 */
@Service
@AllArgsConstructor
@Slf4j
public class RedisService {

    /**
     * RedisTemplate for Redis Operations
     */
    private final RedisTemplate<String,Object> redisTemplate;

    /**
     * Add Data to Redis with TTL
     * @param key : Key to store data
     * @param value : Data to store
     * @param ttl : Time to live in milliseconds
     */
    public void addData(String key, Object value, long ttl){
        redisTemplate.opsForValue()
                .set(key,value,ttl, TimeUnit.MILLISECONDS);
        log.info("Data added to Redis with Key: {}, TTL: {} milliseconds",key, ttl);
    }

    /**
     * Check if Key Exists in Redis
     * @param key : Key to check
     * @return true if key exists, false otherwise
     */
    public boolean isKeyExist(String key){
        boolean exists = redisTemplate.hasKey(key);
        log.info("Key {} {} in Redis",key, exists ? "exists" : "does not exist");
        return exists;
    }


}
