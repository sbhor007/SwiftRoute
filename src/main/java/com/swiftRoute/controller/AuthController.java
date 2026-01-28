package com.swiftRoute.controller;

import com.swiftRoute.annotation.RateLimit;
import com.swiftRoute.records.RefreshRequest;
import com.swiftRoute.records.auth.LoginRequest;
import com.swiftRoute.records.user.RegisterRequest;
import com.swiftRoute.response.ApiResponse;
import com.swiftRoute.service.AuthService;
import com.swiftRoute.service.OTPService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Endpoints for user registration, login, OTP, and profile management")
public class AuthController {
    private final AuthService authService;
    private final OTPService otpService;

    /**
     * User Registration Endpoint
     */
    @RateLimit(limit = 4, ttl = 60)
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

    /**
     * User login endpoint
     */
    @RateLimit(limit = 4, ttl = 60)
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

    /**
     * Refresh access token using valid refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<?>> refresh(@Valid @RequestBody RefreshRequest request){
        try{
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.builder()
                            .Status(HttpStatus.CREATED)
                            .Message("New Access Token generated")
                            .Data(authService.refreshToken(request))
                            .build());
        }catch (Exception e){
            log.error("Refresh token processing failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .Status(HttpStatus.BAD_REQUEST)
                            .Message(e.getMessage())
                            .build()
                    );
        }
    }


    /**
    *User Profile Endpoint
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> userProfile(Authentication authentication){
//        if(authentication == null || !authentication.isAuthenticated()){
//            log.info("Unauthorized access");
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(HttpStatus.UNAUTHORIZED,"Unauthorized user",null));
//        }
        try{
            log.info("Name : {}",authentication.getName());
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(HttpStatus.OK,"user profile retrieve", authService.userProfile(authentication.getName())));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(HttpStatus.BAD_REQUEST,e.getMessage(), null));
        }
    }

    /**
    *Send OTP Endpoint
     */
    @RateLimit(limit = 4, ttl = 60)
    @PostMapping("/send-otp/{email}")
    public ResponseEntity<ApiResponse<?>> sendOtp(@PathVariable String email){
        log.info("Sending OTP to email: {}", email);
        try{
            otpService.sendOtp(email);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(
                    HttpStatus.OK,
                    "OTP send successfully",
                    null
            ));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage(),
                    null
            ));
        }
    }

    /*
    Verify OTP Endpoint
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<?>> verifyOtp(@RequestParam String email,@RequestParam String otp){
        log.info("Verifying OTP for email: {}", email);
            if(otpService.verifyOtp(email,otp)){
                return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(
                        HttpStatus.OK,
                        "Otp Verified Successfully",
                        null
                ));
            }else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(
                        HttpStatus.BAD_REQUEST,
                        "Invalid OTP",
                        null
                ));
            }
    }


}
