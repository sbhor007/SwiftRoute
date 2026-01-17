package com.swiftRoute.records.auth;

import com.swiftRoute.enums.UserRole;

public record LoginRequest(
        String username,
        String password,
        UserRole role
) {
}
