package com.swiftRoute.service;

import com.swiftRoute.annotation.RedisCacheable;
import com.swiftRoute.entity.User;
import com.swiftRoute.enums.UserRole;
import com.swiftRoute.records.RefreshRequest;
import com.swiftRoute.records.auth.LoginRequest;
import com.swiftRoute.records.auth.AuthResponse;
import com.swiftRoute.records.user.RegisterRequest;
import com.swiftRoute.records.user.UserProfileResponse;
import com.swiftRoute.repository.UserRepository;
import com.swiftRoute.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private  final JwtUtil jwtUtil;
    private final TokenService tokenService;
    /**
     * User Registration
     * @param registerRequest
     */
    @Transactional
    public void register(RegisterRequest registerRequest){
        try{
            if(emailAlreadyExist(registerRequest.email(),registerRequest.role())){
                log.info("{} email and his {} role already exist",registerRequest.email(),registerRequest.role());
                throw new RuntimeException("User Already exist");
            }

            User user = User.builder()
                    .name(registerRequest.name())
                    .email(registerRequest.email())
                    .password(passwordEncoder.encode(registerRequest.password()))
                    .role(registerRequest.role())
                    .build();
            log.info("User information : {}",user.getRole());
            userRepository.save(user);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * User Login
     * @param request
     * @return
     */
    public Optional<AuthResponse> login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.username());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            User user = (User) authentication.getPrincipal();
            if(user == null)
                throw new UsernameNotFoundException(STR."User Not Foud with username: \{request.username()}");

            if (user.getRole() != request.role()) {
                log.error("Role mismatch: expected {}, found {}", request.role(), user.getRole());
                throw new RuntimeException("Invalid Credentials");
            }

            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            return Optional.of(AuthResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .tokenType("Bearer")
                            .role(user.getRole())
                            .expiresIn(null)
                            .userEmail(user.getEmail())
                            .build()
                    );

        } catch (Exception e) {
            log.error("Login failed for user: {}", request.username(), e);
            throw new RuntimeException("Invalid Credentials");
        }
    }
    /**
     * Get User Profile from Redis Cache or DB
     * @param username
     * @return UserProfileResponse
     */
    @RedisCacheable(
            key = "'profile:' + #username",
            ttl = 500,
            unit = TimeUnit.SECONDS
    )
    public UserProfileResponse userProfile(String username){
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Refresh JWT Access Token
     * @return Map<String, ?> with new access token details
     */
    public Map<String, ?> refreshToken(RefreshRequest request)throws UsernameNotFoundException {
        String refreshToken = request.refreshToken().trim();
        if (!tokenService.validateToken(refreshToken)) {
            log.warn("Invalid refresh token submitted");
            throw new JwtException("Invalid Refresh token");
        }
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        String tokenRole = jwtUtil.getRoleFromToken(refreshToken);
        UserProfileResponse userProfile = userProfile(username);
        if (!userProfile.role().name().equals(tokenRole)) {
            log.error("Role mismatch during token refresh: token role {}, user role {}", tokenRole, userProfile.role());
            throw new RuntimeException("Invalid Refresh token");
        }
        String newAccessToken = jwtUtil.generateAccessToken(User.builder()
                .id(userProfile.id())
                .role(userProfile.role())
                .email(userProfile.email())
                .build());
        log.info("Access token refreshed for user: {}, :{}", username, newAccessToken);
        return Map.of(
                "accessToken", newAccessToken,
                "tokenType", "Bearer",
                "expiresIn", jwtUtil.getAccessExpirationMs() / 1000
        );
    }

    /**
     * Check if email already exist with role
     * @param email
     * @param role
     * @return boolean
     */
    private boolean emailAlreadyExist(String email, UserRole role){
            log.info("check email and Role already exist");
            return userRepository.existsByEmailAndRole(email,role).orElse(false);
    }

    public void logout(String token, String accessToken) {
        // Invalidate the refresh token (implementation depends on your token management strategy)
        log.info("Logout called for refresh token: {}", token);
        if(!tokenService.validateToken(token)){
            log.info("Invalid Token");
            throw new JwtException("Invalid Token");
        }
        tokenService.blacklistToken(token, accessToken);
//        tokenService.isBlacklistToken(accessToken);
    }
}
