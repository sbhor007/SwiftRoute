package com.swiftRoute.service;

import com.swiftRoute.annotation.RedisCacheable;
import com.swiftRoute.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
@Slf4j
public class CustomeUserService implements UserDetailsService {

    private final UserRepository userRepository;

//    @RedisCacheable(
//            key = "'user:detail:' + #username",
//            ttl = 500,
//            unit = TimeUnit.SECONDS
//    )
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);
        return userRepository.findByEmail(username).orElseThrow();
    }
}
