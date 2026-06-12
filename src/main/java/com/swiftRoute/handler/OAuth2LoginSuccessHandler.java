package com.swiftRoute.handler;

import com.swiftRoute.entity.User;
import com.swiftRoute.enums.UserRole;
import com.swiftRoute.records.auth.AuthResponse;
import com.swiftRoute.repository.UserRepository;
import com.swiftRoute.response.ApiResponse;
import com.swiftRoute.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
@AllArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private UserRepository userRepository;
    private JwtUtil jwtUtil;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        log.info("OAuth2 User: {}",oAuth2User);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        log.info("OAuth2 login success for email: {}", email);

        User user = userRepository.findByEmail(email).orElseGet(() ->{
            log.info("No existing user found for email: {}. Creating new user.", email);
            User newUser = User.builder()
                    .email(email)
                    .name(name)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role(UserRole.CUSTOMER)
                    .enabled(true)
                    .accountLocked(false).build();
                    return newUser;
        });
        userRepository.save(user);
        log.info("Generating tokens for user with email: {} and role: {}", user.getEmail(), user.getRole());
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .role(user.getRole())
                .expiresIn(null)
                .userEmail(user.getEmail())
                .build();
        ApiResponse<AuthResponse> apiResponse = new ApiResponse<>(
                HttpStatus.OK, "User Login successfully", authResponse
        );
        log.info("OAuth2 login completed successfully for user: {}", user.getEmail());

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
