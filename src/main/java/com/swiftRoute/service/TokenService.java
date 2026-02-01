package com.swiftRoute.service;

import com.swiftRoute.util.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@AllArgsConstructor
public class TokenService {
    private final RedisService redisService;
    private final JwtUtil jwtUtil;


    public boolean isBlacklistToken(String token){
        if(redisService.isKeyExist("blacklist:" + token.hashCode())){
            log.info("Token is blacklisted");
            return true;
        }
        log.info("Token is NOT Blacklisted");
        return false;
    }

    public void blacklistToken(String ...tokens){

        if (tokens == null || tokens.length == 0) {
            log.warn("No tokens provided for blacklisting");
            return;
        }

        for (String token : tokens) {
            try {
                if (token == null || token.isBlank()) {
                    continue;
                }

                if (token.startsWith("Bearer ")) {
                    log.warn("Removing 'Bearer ' prefix before blacklisting");
                    token = token.substring(7);
                }
                log.warn("Blacklisting token (hash): {}", token.hashCode());

                long expirationTime = jwtUtil.getExpirationTime(token);
                long now = System.currentTimeMillis();

                log.info("Expiration Date : {}", new Date(expirationTime));
                log.info("Current Date    : {}", new Date(now));

                if (now < expirationTime) {
                    long ttlMs = expirationTime - now;
                    log.info("Token valid, TTL (ms): {}", ttlMs);

                    redisService.addData(
                            "blacklist:" + token.hashCode(),
                            true,
                            ttlMs
                    );
                } else {
                    log.info("Token already expired, skipping blacklist");
                }

            } catch (Exception e) {
                log.error("Error blacklisting token", e);
            }
        }
    }

    public boolean validateToken(String bearerToken) {
        if (bearerToken == null || bearerToken.trim().isEmpty()) {
            log.warn("JWT validation failed: Token is null or empty");
            return false;
        }
        String token;
        if (bearerToken.startsWith("Bearer ")) {
            log.warn("JWT validation: found 'Bearer ' prefix");
            token = bearerToken.substring(7);
//            return false;
        }else {
            token = bearerToken;
        }

        if (isBlacklistToken(token)) {
            log.warn("Token is blacklisted");
            return false;
        }
        return jwtUtil.validateJwtStructure(token);

    }

}
