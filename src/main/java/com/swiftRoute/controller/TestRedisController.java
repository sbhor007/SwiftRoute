package com.swiftRoute.controller;

import com.swiftRoute.service.TestRedisService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@AllArgsConstructor
public class TestRedisController {
    private final TestRedisService testRedisService;

    @GetMapping("/health")
    public String healthCheck(){
        return "Health Check...OK!";
    }

    @GetMapping("/post/{id}")
    public ResponseEntity<?> getPost(@PathVariable int id){
        return ResponseEntity.ok().body(testRedisService.getData(id));
    }
}
