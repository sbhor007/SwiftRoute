package com.swiftRoute.records.user;

import com.swiftRoute.enums.UserRole;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UserProfileResponse(
        UUID id,
        String name,
        String email,
        UserRole role,
        LocalDateTime createdAt
) {
}
