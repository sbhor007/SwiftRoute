package com.swiftRoute.service;

import com.swiftRoute.util.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
@Slf4j
public class RedisService {
    private final RedisTemplate<String,Object> redisTemplate;

    public void addData(String key, Boolean value, long ttl){
        redisTemplate.opsForValue()
                .set(key,value,ttl, TimeUnit.MILLISECONDS);
    }

    public boolean isKeyExist(String key){
        return redisTemplate.hasKey(key);
    }


}
