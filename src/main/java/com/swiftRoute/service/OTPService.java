package com.swiftRoute.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@AllArgsConstructor
public class OTPService {
    private final RedisTemplate<String,String> redisTemplate;
    private final EmailService emailService;

    /*
    Generate OTP
     */
    private String generateOtp(int digits){
        SecureRandom random = new SecureRandom();
        int bound = (int) Math.pow(10, digits);
        log.info("BOUND::{}",bound);
        int num = random.nextInt(bound - bound/10) + bound/10;
        return String.valueOf(num);
    }

    /*
    Send OTP
     */
    public void sendOtp(String email){
        log.info("Generating OTP for email: {}", email);
        String OTP = this.generateOtp(6);
        try{
            this.emailService.sendMail(
                    email,
                    "OTP",
                    "Your OTP is: " + OTP + "It will valide for 5 minutes"
            );
            String key = "OTP:" + email;
            log.info("Storing OTP in Redis with key: {}", key);
            redisTemplate.opsForValue()
                    .set(
                      key,
                      OTP,
                       300,
                       TimeUnit.SECONDS
                    );
            log.info("OTP stored in Redis successfully");
        }catch (Exception e){
            log.error("Error sending OTP: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /*
    Verify OTP
     */
    public boolean verifyOtp(String email,String OTP){
        String key = "OTP:" + email;
        Object cache = redisTemplate.opsForValue().get(key);
        if(cache != null){
            log.info("Cache OTP available");
            if(cache.equals(OTP)) {
                redisTemplate.opsForValue().getAndDelete(key);
                return true;
            }
        }
        log.info("Cache OTP not available");
        return false;
    }

}
