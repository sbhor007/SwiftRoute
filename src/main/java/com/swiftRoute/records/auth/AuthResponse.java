package com.swiftRoute.records.auth;

import com.swiftRoute.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record AuthResponse(
        @NotBlank
        String accessToken,
        @NotBlank
        String refreshToken,
        @NotBlank
        UserRole role,
        String tokenType,
        Long expiresIn,
        String userEmail,
        String userRole
) {

}
