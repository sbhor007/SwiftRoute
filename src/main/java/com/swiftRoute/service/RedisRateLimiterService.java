package com.swiftRoute.service;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class RedisRateLimiterService {
    private final RedisTemplate<String,Object> redisTemplate;
    /**
     * Rate Limiter using Redis Sorted Set
     * @param key Unique key for the user/requester
     * @param limitRequests Maximum number of requests allowed
     * @param ttl Time window in seconds
     * @return true if request is allowed, false otherwise
     */
    public boolean allowRequest(String key, int limitRequests, int ttl){

        /**remove old request*/
        long now  = System.currentTimeMillis();
        redisTemplate.opsForZSet()
                .removeRangeByScore(key,0,now-ttl * 1000L);

        /** Count Number of Requests */
        Long count = redisTemplate.opsForZSet().zCard(key);
        if(count != null && count >= limitRequests){
            return false;
        }

        /** add current request */
        redisTemplate.opsForZSet()
                .add(key,String.valueOf(now),now);

        /** set expiry */
        redisTemplate.expire(key,ttl, TimeUnit.SECONDS);

        return true;
    }
}
