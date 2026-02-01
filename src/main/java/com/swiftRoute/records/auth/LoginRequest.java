package com.swiftRoute.records.auth;

import com.swiftRoute.enums.UserRole;
import jakarta.validation.constraints.*;

public record LoginRequest(
        @Email
        @NotBlank(message = "Username Must be required")
        String username,
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
                message = "Password must contain at least one digit, one upper case, one lower case, and one special character"
        )
        String password,
        @NotNull
        UserRole role
) {
}
