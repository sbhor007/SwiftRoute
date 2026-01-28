package com.swiftRoute.response;

import lombok.*;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class ApiResponse<T> {
    private HttpStatus Status;
    private String Message;
    private T Data;
    }
