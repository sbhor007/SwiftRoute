package com.swiftRoute.records.auth;

import com.swiftRoute.enums.UserRole;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record LoginResponse(
        @NotBlank
        String accessToken,
        @NotBlank
        String refreshToken,
        @NotBlank
        UserRole role
) {

}
