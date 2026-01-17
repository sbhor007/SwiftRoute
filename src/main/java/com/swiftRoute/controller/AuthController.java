package com.swiftRoute.controller;

import com.swiftRoute.records.user.RegisterRequest;
import com.swiftRoute.service.AuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest){
        try{
            log.info("User Registration Request: {}",registerRequest);
            authService.register(registerRequest);
            return ResponseEntity.ok().body("User Register successfully");
        }catch (Exception e){
            return ResponseEntity.badRequest()
                    .body("User registration failed: " + e.getMessage());
        }

    }
}
