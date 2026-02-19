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

/**
 * Service class responsible for handling user authentication, registration, and token management.
 * <p>
 * This service provides methods for:
 * <ul>
 *     <li>User registration</li>
 *     <li>User login and authentication</li>
 *     <li>Retrieving user profiles (with caching)</li>
 *     <li>Refreshing JWT access tokens</li>
 *     <li>User logout and token invalidation</li>
 * </ul>
 */
@Service
@Slf4j
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final DriverService driverService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private  final JwtUtil jwtUtil;
    private final TokenService tokenService;
    /**
     * Registers a new user in the system.
     * <p>
     * Checks if the email is already in use for the specified role. If not, creates a new
     * user entity, encodes the password, and saves it to the database.
     *
     * @param registerRequest The request object containing user registration details (name, email, password, role).
     * @throws RuntimeException If the user with the given email and role already exists, or if any other error occurs.
     */
    @Transactional
    public void register(RegisterRequest registerRequest){
        try{
            if(emailAlreadyExist(registerRequest.email(),registerRequest.role())) {
                log.info("Registration failed for user with email {} and role {}. User already exists.",
                        registerRequest.email(), registerRequest.role());
                throw new RuntimeException("User already exists");
            }

            User user = User.builder()
                    .name(registerRequest.name())
                    .email(registerRequest.email())
                    .password(passwordEncoder.encode(registerRequest.password()))
                    .role(registerRequest.role())
                    .build();
            log.info("Registered user with email {} and role {}.", user.getEmail(), user.getRole());
            userRepository.save(user);

            if (registerRequest.role() == UserRole.DRIVER) {
                driverService.createDriver(user);
            }
        } catch (Exception e) {
            log.error("Registration failed for user with email {} and role {}: {}",
                    registerRequest.email(), registerRequest.role(), e.getMessage());
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Authenticates a user and generates access and refresh tokens.
     * <p>
     * Validates the provided credentials using {@link AuthenticationManager}. Checks if the user exists
     * and if the role matches. Upon successful authentication, generates a JWT access token and a refresh token.
     *
     * @param request The login request containing the username, password, and role.
     * @return An {@link Optional} containing the {@link AuthResponse} with tokens if successful.
     * @throws UsernameNotFoundException If the user is not found.
     * @throws RuntimeException          If the role mismatches or authentication fails.
     */
    public Optional<AuthResponse> login(LoginRequest request) {
        log.info("Login attempt for user with email {} and role {}", request.username(), request.role());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            User user = (User) authentication.getPrincipal();
            if(user == null) {
                log.warn("User not found with email {}", request.username());
                throw new UsernameNotFoundException("User not found with email " + request.username());
            }

            if (user.getRole() != request.role()) {
                log.warn("Role mismatch: expected {}, found {}", request.role(), user.getRole());
                throw new RuntimeException("Invalid credentials. Role mismatch");
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

        } catch (UsernameNotFoundException e) {
            log.error("Login failed for user with email {}: {}", request.username(), e.getMessage());
            throw new RuntimeException("Invalid credentials. User not found");
        } catch (Exception e) {
            log.error("Login failed for user with email {}: {}", request.username(), e.getMessage());
            throw new RuntimeException("Invalid credentials: " + e.getMessage());
        }
    }
    /**
     * Retrieves the user profile information.
     * <p>
     * This method is cacheable using Redis. It tries to fetch the profile from the cache first;
     * if not found, it retrieves it from the database and caches the result.
     *
     * @param username The email/username of the user to retrieve.
     * @return The {@link UserProfileResponse} containing user details.
     * @throws UsernameNotFoundException If the user is not found in the database.
     */
    @RedisCacheable(
            key = "'profile:' + #username",
            ttl = 500,
            unit = TimeUnit.SECONDS
    )
    public UserProfileResponse userProfile(String username) {
        log.info("Retrieving user profile for username: {}", username);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    log.error("User not found with username: {}", username);
                    return new UsernameNotFoundException("User not found");
                });
        log.info("User profile retrieved successfully for username: {}", username);
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Refreshes the JWT access token using a valid refresh token.
     * <p>
     * Validates the provided refresh token. If valid and not expired/blacklisted, generates a new access token.
     * Also checks for consistency between the token's role and the user's current role.
     *
     * @param request The request object containing the refresh token.
     * @return A {@link Map} containing the new access token, token type, and expiration time.
     * @throws JwtException     If the refresh token is invalid or expired.
     * @throws RuntimeException If there is a role mismatch.
     */
    public Map<String, ?> refreshToken(RefreshRequest request) throws UsernameNotFoundException {
        String refreshToken = request.refreshToken().trim();
        if (!tokenService.validateToken(refreshToken)) {
            log.warn("Refresh token validation failed for token: {}", refreshToken);
            throw new JwtException("Invalid Refresh token");
        }
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        String tokenRole = jwtUtil.getRoleFromToken(refreshToken);
        UserProfileResponse userProfile = userProfile(username);
        if (!userProfile.role().name().equals(tokenRole)) {
            log.error("Role mismatch during token refresh: token role {}, user role {}", tokenRole, userProfile.role());
            throw new RuntimeException("Invalid Refresh token - Role mismatch");
        }
        String newAccessToken = jwtUtil.generateAccessToken(User.builder()
                .id(userProfile.id())
                .role(userProfile.role())
                .email(userProfile.email())
                .build());
        log.info("Access token refreshed successfully for user: {}, new token: {}", username, newAccessToken);
        return Map.of(
                "accessToken", newAccessToken,
                "tokenType", "Bearer",
                "expiresIn", jwtUtil.getAccessExpirationMs() / 1000
        );
    }

    /**
     * Checks if a user with the given email and role already exists in the database.
     *
     * @param email The email address to check.
     * @param role  The user role to check.
     * @return {@code true} if a user exists with the specified email and role, {@code false} otherwise.
     */
    private boolean emailAlreadyExist(String email, UserRole role){
        log.info("Checking if email: {} and role: {} already exist", email, role);
        try{
            boolean exists = userRepository.existsByEmailAndRole(email,role).orElseThrow(
                    () -> new RuntimeException("Error checking if email and role exist")
            );
            log.debug("Email and role exist: {}", exists);
            return exists;
        } catch (Exception e){
            log.error("Error checking if email and role exist: {}", e.getMessage());
            throw new RuntimeException("Error checking if email and role exist: " + e.getMessage());
        }
    }

    /**
     * Logs out a user by invalidating their tokens.
     * <p>
     * Validates the provided refresh token and adds both the refresh token and the access token
     * to the blacklist to prevent further use.
     *
     * @param token       The refresh token to validate and blacklist.
     * @param accessToken The access token to blacklist.
     * @throws JwtException If the refresh token is invalid.
     */
    public void logout(String token, String accessToken) {
        // Invalidate the refresh token (implementation depends on your token management strategy)
        log.info("Attempting to logout for refresh token: {}", token);
        if (!tokenService.validateToken(token)) {
            log.warn("Invalid Token provided. Logout failed");
            throw new JwtException("Invalid Refresh token. Logout failed");
        }
        try {
            tokenService.blacklistToken(token, accessToken);
            log.info("Logout successful");
        } catch (Exception e) {
            log.error("Error occurred during token blacklisting. Logout failed: {}", e.getMessage());
            throw new RuntimeException("Error occurred during token blacklisting. Logout failed: " + e.getMessage());
        }
    }
}
