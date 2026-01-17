package com.swiftRoute.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class DriverService {


    public static String generateCode() {
        long timestamp = System.currentTimeMillis();
        int random = new SecureRandom().nextInt(1000);
        return Long.toString(timestamp, 36).toUpperCase().substring(4) + random;
    }
}
