package com.swiftRoute.ratelimit;

import com.swiftRoute.service.RedisRateLimiterService;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RateLimitTest {
    @Mock
    private RedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String,String> zSetOps;

    @InjectMocks
    private RedisRateLimiterService rateLimiterService;

    @BeforeEach
    void setup() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
    }

    @Test
    void shouldAllowRequestWhenUnderLimit() {

        String key = "rate_limit:test";

        when(zSetOps.zCard(key)).thenReturn(1L);

        boolean allowed = rateLimiterService.allowRequest(key, 2, 10);

        assertTrue(allowed);
    }

    @Test
    void shouldBlockRequestWhenLimitExceeded() {

        String key = "rate_limit:test";

        when(zSetOps.zCard(key)).thenReturn(2L);

        boolean allowed = rateLimiterService.allowRequest(key, 2, 10);

        assertFalse(allowed);
    }

    @Test
    void shouldAddTimestampWhenAllowed() {

        String key = "rate_limit:test";

        when(zSetOps.zCard(key)).thenReturn(0L);

        rateLimiterService.allowRequest(key, 2, 10);

        verify(zSetOps).add(eq(key), anyString(), anyDouble());
    }

}
