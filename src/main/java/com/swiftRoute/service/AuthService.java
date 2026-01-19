package com.swiftRoute.service;

import com.swiftRoute.annotation.RedisCacheable;
import com.swiftRoute.entity.User;
import com.swiftRoute.enums.UserRole;
import com.swiftRoute.records.auth.LoginRequest;
import com.swiftRoute.records.auth.LoginResponse;
import com.swiftRoute.records.user.RegisterRequest;
import com.swiftRoute.records.user.UserProfileResponse;
import com.swiftRoute.repository.UserRepository;
import com.swiftRoute.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    /*
    User Registration
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
    /*
    User Login based on UserRole
     */
    public Optional<LoginResponse> login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.username());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            User user = (User) authentication.getPrincipal();

            if (user == null || user.getRole() != request.role()) {
                assert user != null;
                log.error("Role mismatch: expected {}, found {}", request.role(), user.getRole());
                throw new RuntimeException("Invalid Credentials");
            }

            String token = jwtUtil.generatAccessToken(user);
            return Optional.of(new LoginResponse(token, user.getRole()));

        } catch (Exception e) {
            log.error("Login failed for user: {}", request.username(), e);
            throw new RuntimeException("Invalid Credentials");
        }
    }
    /*
    Get User Profile
     */
    @RedisCacheable(
            key = "'profile:' + #username",
            ttl = 500,
            unit = TimeUnit.SECONDS
    )
    public UserProfileResponse userProfile(String username){
        User user = userRepository.findByEmail(username).orElse(null);
        if(user == null)
            throw new RuntimeException("User Profile not found");
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }

    private boolean emailAlreadyExist(String email, UserRole role){
            log.info("check email and Role already exist");
            return userRepository.existsByEmailAndRole(email,role).orElse(false);
    }
}
