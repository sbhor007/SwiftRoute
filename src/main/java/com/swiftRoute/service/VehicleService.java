package com.swiftRoute.service;

import com.swiftRoute.base.CrudService;
import com.swiftRoute.entity.Driver;
import com.swiftRoute.entity.Vehicle;
import com.swiftRoute.records.vehicle.VehicleRequest;
import com.swiftRoute.repository.DriverRepository;
import com.swiftRoute.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class VehicleService implements CrudService<Vehicle, UUID> {
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;


    @Override
    public Vehicle add(Vehicle vehicle) {
        log.info("Adding new vehicle with number: {}", vehicle.getVehicleNumber());
        return vehicleRepository.save(vehicle);
    }


    @Override
    public Vehicle update(Vehicle entity) {

        return null;
    }

    @Override
    public void deleteById(UUID uuid) {
            vehicleRepository.deleteById(uuid);
    }

    @Override
    public Vehicle getById(UUID uuid) {
        return vehicleRepository.findById(uuid).orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + uuid));
    }

    @Override
    public List<Vehicle> getAll() {
        return List.of(vehicleRepository.findAll().toArray(new Vehicle[0]));
    }

    // In VehicleService
    @Transactional
    public Vehicle addVehicleForDriver(UUID driverId, VehicleRequest vehicleRequest) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        // Validations
        if (driver.getAssignedVehicle() != null) {
            throw new RuntimeException("Driver already has a vehicle assigned");
        }

        if (vehicleRepository.existsByVehicleNumber(vehicleRequest.vehicleNumber())) {
            throw new RuntimeException("Vehicle number already registered");
        }

        // Create vehicle
        Vehicle vehicle = Vehicle.builder()
                .vehicleNumber(vehicleRequest.vehicleNumber())
                .model(vehicleRequest.model())
                .capacity(vehicleRequest.capacity())
                .isActive(true)
                .isAssigned(true)
                .build();

        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        // Assign to driver
        driver.setAssignedVehicle(savedVehicle);
        driverRepository.save(driver);

        return savedVehicle;
    }

    public void removeVehicleFromDriver(UUID driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        Vehicle vehicle = driver.getAssignedVehicle();
        if (vehicle == null) {
            throw new RuntimeException("Driver does not have a vehicle assigned");
        }

        // Unassign vehicle from driver
        driver.setAssignedVehicle(null);
        driverRepository.save(driver);

        // Optionally, you can also delete the vehicle or mark it as unassigned
        vehicle.setIsAssigned(false);
        vehicleRepository.delete(vehicle);
    }
}
