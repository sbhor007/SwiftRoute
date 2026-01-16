package com.swiftRoute.service;

import com.swiftRoute.annotation.RedisCacheable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
@Slf4j
public class TestRedisService {
    private final WebClient webClient;

    @RedisCacheable(
            key = "'post:' + #id",
            ttl = 5,
            unit = TimeUnit.MINUTES
    )
    public Object getData(int id){
        Object object = webClient.get()
                .uri("https://jsonplaceholder.typicode.com/posts/" + id)
                .retrieve()
                .bodyToMono(Object.class)
                .block();
        log.info("Fetch Data : {}",object);
        return object;
    }


}
