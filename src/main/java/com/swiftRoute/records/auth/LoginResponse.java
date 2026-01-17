package com.swiftRoute.records.auth;

import com.swiftRoute.enums.UserRole;

import java.util.UUID;

public record LoginResponse(
        String token,
        UserRole role
) {

}
