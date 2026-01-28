package com.swiftRoute.records;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record RefreshRequest(
        @NotBlank
        @Size(min = 100)
        String refreshToken
        ) {
}
