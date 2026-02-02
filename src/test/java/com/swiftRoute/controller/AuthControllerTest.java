package com.swiftRoute.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftRoute.enums.UserRole;
import com.swiftRoute.records.RefreshRequest;
import com.swiftRoute.records.auth.AuthResponse;
import com.swiftRoute.records.auth.LoginRequest;
import com.swiftRoute.records.user.RegisterRequest;
import com.swiftRoute.records.user.UserProfileResponse;
import com.swiftRoute.service.AuthService;
import com.swiftRoute.service.OTPService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * JUnit test class for AuthController
 * Tests all authentication endpoints including registration, login, OTP, profile, refresh token, and logout
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @Mock
    private OTPService otpService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    // ==================== REGISTER ENDPOINT TESTS ====================

    @Test
    @DisplayName("Register - Should successfully register a new user")
    void testRegister_Success() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest(
                "John Doe",
                "john.doe@example.com",
                "Password@123",
                UserRole.DRIVER
        );

        doNothing().when(authService).register(any(RegisterRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Status").value("OK"))
                .andExpect(jsonPath("$.Message").value("User Register Cussessfully"));

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Register - Should fail when user already exists")
    void testRegister_UserAlreadyExists() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest(
                "John Doe",
                "john.doe@example.com",
                "Password@123",
                UserRole.DRIVER
        );

        doThrow(new RuntimeException("User already exists"))
                .when(authService).register(any(RegisterRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.Status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.Message").exists());

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    // ==================== LOGIN ENDPOINT TESTS ====================

    @Test
    @DisplayName("Login - Should successfully login with valid credentials")
    void testLogin_Success() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(
                "john.doe@example.com",
                "Password@123",
                UserRole.DRIVER
        );

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("access-token-123")
                .refreshToken("refresh-token-123")
                .tokenType("Bearer")
                .role(UserRole.DRIVER)
                .userEmail("john.doe@example.com")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(Optional.of(authResponse));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Status").value("OK"))
                .andExpect(jsonPath("$.Message").value("User Login successfully"))
                .andExpect(jsonPath("$.Data.accessToken").value("access-token-123"))
                .andExpect(jsonPath("$.Data.refreshToken").value("refresh-token-123"))
                .andExpect(jsonPath("$.Data.tokenType").value("Bearer"));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Login - Should fail with invalid credentials")
    void testLogin_InvalidCredentials() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(
                "john.doe@example.com",
                "WrongPassword@123",
                UserRole.DRIVER
        );

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.Status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.Message").value("Invalid credentials"));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    // ==================== REFRESH TOKEN ENDPOINT TESTS ====================

    @Test
    @DisplayName("Refresh - Should successfully refresh access token")
    void testRefresh_Success() throws Exception {
        // Arrange
        RefreshRequest refreshRequest = RefreshRequest.builder()
                .refreshToken("valid-refresh-token-123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890")
                .build();

        Map<String, Object> refreshResponse = Map.of(
                "accessToken", "new-access-token-123",
                "tokenType", "Bearer",
                "expiresIn", 3600
        );

//        when(authService.refreshToken(any(RefreshRequest.class)))
//                .thenReturn(refreshResponse)

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Status").value("CREATED"))
                .andExpect(jsonPath("$.Message").value("New Access Token generated"))
                .andExpect(jsonPath("$.Data.accessToken").value("new-access-token-123"))
                .andExpect(jsonPath("$.Data.tokenType").value("Bearer"));

        verify(authService, times(1)).refreshToken(any(RefreshRequest.class));
    }

    @Test
    @DisplayName("Refresh - Should fail with invalid refresh token")
    void testRefresh_InvalidToken() throws Exception {
        // Arrange
        RefreshRequest refreshRequest = RefreshRequest.builder()
                .refreshToken("invalid-refresh-token-123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890")
                .build();

        when(authService.refreshToken(any(RefreshRequest.class)))
                .thenThrow(new RuntimeException("Invalid Refresh token"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.Status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.Message").value("Invalid Refresh token"));

        verify(authService, times(1)).refreshToken(any(RefreshRequest.class));
    }

    // ==================== USER PROFILE ENDPOINT TESTS ====================

    @Test
    @DisplayName("User Profile - Should successfully retrieve user profile")
    void testUserProfile_Success() throws Exception {
        // Arrange
        String userEmail = "john.doe@example.com";
        UserProfileResponse profileResponse = UserProfileResponse.builder()
                .name("John Doe")
                .email(userEmail)
                .role(UserRole.DRIVER)
                .createdAt(LocalDateTime.now())
                .build();

        when(authentication.getName()).thenReturn(userEmail);
        when(authService.userProfile(userEmail)).thenReturn(profileResponse);

        // Act & Assert
        mockMvc.perform(get("/api/auth/me")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Status").value("OK"))
                .andExpect(jsonPath("$.Message").value("user profile retrieve"))
                .andExpect(jsonPath("$.Data.email").value(userEmail))
                .andExpect(jsonPath("$.Data.name").value("John Doe"));

        verify(authService, times(1)).userProfile(userEmail);
    }

    @Test
    @DisplayName("User Profile - Should handle user not found")
    void testUserProfile_UserNotFound() throws Exception {
        // Arrange
        String userEmail = "nonexistent@example.com";

        when(authentication.getName()).thenReturn(userEmail);
        when(authService.userProfile(userEmail))
                .thenThrow(new UsernameNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(get("/api/auth/me")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.Message").value("User not found"));

        verify(authService, times(1)).userProfile(userEmail);
    }

    // ==================== SEND OTP ENDPOINT TESTS ====================

    @Test
    @DisplayName("Send OTP - Should successfully send OTP to email")
    void testSendOtp_Success() throws Exception {
        // Arrange
        String email = "john.doe@example.com";
        doNothing().when(otpService).sendOtp(email);

        // Act & Assert
        mockMvc.perform(post("/api/auth/send-otp/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Status").value("OK"))
                .andExpect(jsonPath("$.Message").value("OTP send successfully"));

        verify(otpService, times(1)).sendOtp(email);
    }

    @Test
    @DisplayName("Send OTP - Should handle failure when sending OTP")
    void testSendOtp_Failure() throws Exception {
        // Arrange
        String email = "john.doe@example.com";
        doThrow(new RuntimeException("Email service unavailable"))
                .when(otpService).sendOtp(email);

        // Act & Assert
        mockMvc.perform(post("/api/auth/send-otp/{email}", email))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.Status").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.Message").value("Email service unavailable"));

        verify(otpService, times(1)).sendOtp(email);
    }

    // ==================== VERIFY OTP ENDPOINT TESTS ====================

    @Test
    @DisplayName("Verify OTP - Should successfully verify valid OTP")
    void testVerifyOtp_Success() throws Exception {
        // Arrange
        String email = "john.doe@example.com";
        String otp = "123456";

        when(otpService.verifyOtp(email, otp)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/auth/verify-otp")
                        .param("email", email)
                        .param("otp", otp))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Status").value("OK"))
                .andExpect(jsonPath("$.Message").value("Otp Verified Successfully"));

        verify(otpService, times(1)).verifyOtp(email, otp);
    }

    @Test
    @DisplayName("Verify OTP - Should fail with invalid OTP")
    void testVerifyOtp_InvalidOtp() throws Exception {
        // Arrange
        String email = "john.doe@example.com";
        String otp = "999999";

        when(otpService.verifyOtp(email, otp)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/auth/verify-otp")
                        .param("email", email)
                        .param("otp", otp))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.Status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.Message").value("Invalid OTP"));

        verify(otpService, times(1)).verifyOtp(email, otp);
    }

    // ==================== LOGOUT ENDPOINT TESTS ====================

    @Test
    @DisplayName("Logout - Should successfully logout user")
    void testLogout_Success() throws Exception {
        // Arrange
        String refreshToken = "valid-refresh-token";
        String accessToken = "Bearer valid-access-token";

        doNothing().when(authService).logout(refreshToken, accessToken);

        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                        .param("refreshToken", refreshToken)
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Status").value("OK"))
                .andExpect(jsonPath("$.Message").value("User logged out successfully"));

        verify(authService, times(1)).logout(refreshToken, accessToken);
    }

    @Test
    @DisplayName("Logout - Should fail with invalid refresh token")
    void testLogout_InvalidToken() throws Exception {
        // Arrange
        String refreshToken = "invalid-refresh-token";
        String accessToken = "Bearer valid-access-token";

        doThrow(new RuntimeException("Invalid Refresh token"))
                .when(authService).logout(refreshToken, accessToken);

        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                        .param("refreshToken", refreshToken)
                        .header("Authorization", accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.Status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.Message").value("Invalid Refresh token"));

        verify(authService, times(1)).logout(refreshToken, accessToken);
    }
}
