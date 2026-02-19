package com.swiftRoute.records.vehicle;

public record VehicleRequest(
        String vehicleNumber,
        String model,
        Integer capacity
) {
}
