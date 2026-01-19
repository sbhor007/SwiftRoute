package com.swiftRoute.aspect;

import com.swiftRoute.annotation.RateLimit;
import com.swiftRoute.response.ApiResponse;
import com.swiftRoute.service.RedisRateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;


@Aspect
@Component
@AllArgsConstructor
public class RateLimitAspect {
    private final RedisRateLimiterService rateLimiterService;

    @Around("@annotation(rateLimit)")
    public Object applyRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder
                .currentRequestAttributes())
                .getRequest();
        String clientId = request.getRemoteAddr();
        String redisKey = "rate_limit:" + clientId;
        boolean allowed = rateLimiterService.allowRequest(
                redisKey,
                rateLimit.limit(),
                rateLimit.ttl()
        );

        if(!allowed){
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ApiResponse<>(HttpStatus.TOO_MANY_REQUESTS, "Too many requests", null));
        }

        return joinPoint.proceed();
    }
}
