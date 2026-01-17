package com.swiftRoute.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void cleanRedis() {
        Set<String> keys = redisTemplate.keys("rate_limit:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    void shouldStoreDataInRedis() throws Exception {

        // Call API
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk());

        // NOW Redis actually has data
        Set<String> keys = redisTemplate.keys("rate_limit:*");

        assertNotNull(keys);
        assertFalse(keys.isEmpty());

        // Print Redis content (for learning)
        for (String key : keys) {
            System.out.println("Redis Key: " + key);
            System.out.println(
                    redisTemplate.opsForZSet().rangeWithScores(key, 0, -1)
            );
        }
    }
}

