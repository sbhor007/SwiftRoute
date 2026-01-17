package com.swiftRoute.controller;

import com.swiftRoute.annotation.RateLimit;
import com.swiftRoute.records.auth.LoginRequest;
import com.swiftRoute.records.user.RegisterRequest;
import com.swiftRoute.response.ApiResponse;
import com.swiftRoute.service.AuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(HttpStatus.OK,"User Register Cussessfully",null));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(HttpStatus.BAD_REQUEST, STR."User Registration Fail : \{e.getMessage()}",null));
        }

    }

    @RateLimit(limit = 2, window = 100)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequest loginRequest){
        try {
            log.info("Login attempt for user: {}", loginRequest.toString());
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(HttpStatus.OK, "User Login successfully",
                    authService.login(loginRequest)));
        } catch (Exception e) {
            log.error("Login failed for user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<String>(HttpStatus.BAD_REQUEST, e.getMessage(),null));
        }
    }
}
