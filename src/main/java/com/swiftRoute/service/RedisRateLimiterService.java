package com.swiftRoute.service;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class RedisRateLimiterService {
    private final RedisTemplate redisTemplate;

    public boolean allowRequest(String key, int limitRequests, int windowSeconds){

        /*remove old request*/
        long now  = System.currentTimeMillis();
        redisTemplate.opsForZSet()
                .removeRangeByScore(key,0,now-windowSeconds * 1000L);

        /* Count Number of Requests */
        Long count = redisTemplate.opsForZSet().zCard(key);
        if(count != null && count >= limitRequests){
            return false;
        }

        /* add current request */
        redisTemplate.opsForZSet()
                .add(key,String.valueOf(now),now);

        /* set expiry */
        redisTemplate.expire(key,windowSeconds, TimeUnit.SECONDS);

        return true;
    }
}
