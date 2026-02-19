package com.swiftRoute.records.driver;

import com.swiftRoute.enums.DriverStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DriverCreateRequest(
//        @NotBlank
//         String driverCode,

                @NotBlank
                 String name,

                @NotBlank
                 String phone,

                @NotNull
                 DriverStatus status
){


}
