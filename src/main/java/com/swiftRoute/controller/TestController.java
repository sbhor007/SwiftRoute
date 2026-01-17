package com.swiftRoute.controller;

import com.swiftRoute.annotation.RateLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class TestController {
    @RateLimit(limit = 2, window = 10)
    @GetMapping("/hello")
    public String hello() {
        return "Hello from Annotation Rate Limiter!";
    }
}
