package com.swiftRoute.records.user;

import lombok.Builder;

@Builder
public record UpdateUserRequest(
       String name
) {
}
