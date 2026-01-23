package com.swiftRoute.controller;

import com.swiftRoute.annotation.RateLimit;
import com.swiftRoute.response.ApiResponse;
import com.swiftRoute.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class TestController {
    @Autowired
    private EmailService emailService;

    @RateLimit(limit = 2, ttl = 60)
    @GetMapping("/hello")
    public String hello() {
        return "Hello from Annotation Rate Limiter!";
    }

    @PostMapping("/send/mail")
    public ResponseEntity<ApiResponse<?>> sendMail(){
        try {
            emailService.sendMail(
                    "sbhor747@gmail.com",
                    "Hello",
                    "This is Test Mail"
            );
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ApiResponse<>(
                            HttpStatus.OK,
                            "Mail send Successfully",
                            null
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            new ApiResponse<>(
                                    HttpStatus.INTERNAL_SERVER_ERROR,
                                    e.getMessage(),
                                    null
                            )
                    );
        }
    }
}
