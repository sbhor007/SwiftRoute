package com.swiftRoute.controller;

import com.swiftRoute.entity.User;
import com.swiftRoute.entity.Vehicle;
import com.swiftRoute.records.driver.DriverCreateRequest;
import com.swiftRoute.records.vehicle.VehicleRequest;
import com.swiftRoute.response.ApiResponse;
import com.swiftRoute.service.CustomeUserService;
import com.swiftRoute.service.DriverService;
import com.swiftRoute.service.VehicleService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/driver")
@Slf4j
@AllArgsConstructor
public class DriverController {

    private final DriverService driverService;
    private final VehicleService vehicleService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<?>> getProfile(Authentication authentication){
        try{
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                            .Status(HttpStatus.OK)
                            .Message("User Profile retried successfully")
                            .Data(driverService.getProfile(authentication.getName()))
                            .build()
            );
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.builder()
                    .Status(HttpStatus.NOT_FOUND)
                    .Message(e.getMessage())
                    .build());
        }
    }

    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<?>> updateProfile(
            @Valid @RequestBody DriverCreateRequest request,
            Authentication authentication
    ){
        try{
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                    .Status(HttpStatus.OK)
                    .Message("User Profile Update successfully")
                    .Data(driverService.updateDriver(request,authentication.getName()))
                    .build()
            );
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                    .Status(HttpStatus.BAD_REQUEST)
                    .Message(e.getMessage())
                    .build());
        }
    }

    @PostMapping("/{driverId}/vehicle")
    public ResponseEntity<ApiResponse<?>> addVehicle(
            @PathVariable UUID driverId,
            @RequestBody @Valid VehicleRequest request
            ) {
        try{
            Vehicle vehicle = vehicleService.addVehicleForDriver(driverId, request);
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                    .Status(HttpStatus.OK)
                    .Message("Vehicle added successfully")
                    .Data(vehicle)
                    .build()
            );
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                    .Status(HttpStatus.BAD_REQUEST)
                    .Message(e.getMessage())
                    .build());
        }

    }
    @DeleteMapping("/{driverId}/vehicle")
    public ResponseEntity<ApiResponse<?>> removeVehicle(@PathVariable UUID driverId) {
        vehicleService.removeVehicleFromDriver(driverId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                .Status(HttpStatus.OK)
                .Message("Vehicle removed successfully")
                .build());
    }
}
