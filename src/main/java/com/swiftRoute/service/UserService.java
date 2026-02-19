package com.swiftRoute.service;

import com.swiftRoute.annotation.RedisCacheable;
import com.swiftRoute.entity.User;
import com.swiftRoute.records.user.UserProfileResponse;
import com.swiftRoute.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    @RedisCacheable(
            key = "'user:profile:' + #email",
            ttl = 900,
            unit = TimeUnit.SECONDS
    )
    public UserProfileResponse userProfile(String email) {
        log.info("Retrieving user profile for username: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with username: {}", email);
                    return new UsernameNotFoundException("User not found");
                });
        log.info("User profile retrieved successfully for username: {}", email);
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

}
