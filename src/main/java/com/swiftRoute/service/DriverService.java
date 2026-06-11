package com.swiftRoute.service;

import com.swiftRoute.annotation.RedisCachePut;
import com.swiftRoute.annotation.RedisCacheable;
import com.swiftRoute.base.CrudService;
import com.swiftRoute.entity.Driver;
import com.swiftRoute.entity.User;
import com.swiftRoute.enums.DriverStatus;
import com.swiftRoute.records.driver.DriverCreateRequest;
import com.swiftRoute.repository.DriverRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
@Slf4j
public class DriverService implements CrudService<Driver,UUID> {

    private final DriverRepository driverRepository;


    public Driver add(Driver entity) {
        return driverRepository.save(entity);
    }

    @RedisCacheable(
            key = "'profile:driver:' + #email",
            ttl = 900,
            unit = TimeUnit.SECONDS
    )
    public Driver getProfile(String email){
        log.info("Driver email : {}",email);
        return  driverRepository.findDriverByUserEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    @RedisCacheable(
            key = "'profile:driver' + #id",
            ttl = 900,
            unit = TimeUnit.SECONDS
    )
    public Driver getById(UUID id) {
        log.info("Driver id : {}",id);
        return driverRepository.findById(id).orElseThrow(() -> new RuntimeException("Driver not found with id: " + id));
    }

    @RedisCacheable(
            key = "'profile:driver:' + #driverCode",
            ttl = 900,
            unit = TimeUnit.SECONDS
    )
    public Driver getProfileByDriverCode(String driverCode){
        log.info("Driver driverCode : {}",driverCode);
        return  driverRepository.findByDriverCode(driverCode).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }


    @Transactional
    public void createDriver(User user){
        Driver driver = Driver.builder()
                .driverCode(this.generateCode())
                .name(user.getName())
                .phone("")
                .user(user)
                .status(DriverStatus.OFFLINE) // Or whatever initial status
                .isAvailable(false)
                .isVerified(true)
                .build();
        this.add(driver);
        log.info("Created driver profile for user with email {}.", user.getEmail());
    }


    @RedisCachePut(
            key = "'profile:driver:' + #email",
            ttl = 900,
            unit = TimeUnit.SECONDS
    )
    @Transactional
    public Driver updateDriver(DriverCreateRequest request, String email){
        if(request == null){
            throw new RuntimeException("Request is NULL");
        }
        Driver driver = this.getProfile(email);
        if(request.name() != null){
            driver.setName(request.name());
            driver.getUser().setName(request.name());
        }

        if(request.phone() != null){
            driver.setPhone(request.phone());
        }
        if(request.status() != null){
            driver.setStatus(request.status());
        }

        this.update(driver);

        return driver;
    }

    private String generateCode() {
        long timestamp = System.currentTimeMillis();
        int random = new SecureRandom().nextInt(1000);
        return Long.toString(timestamp, 36).toUpperCase().substring(4) + random;
    }



    @Override
    public Driver update(Driver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("Driver is null");
        }
        return driverRepository.save(driver);
    }

    @Override
    public void deleteById(UUID uuid) throws RuntimeException {
        driverRepository.deleteById(uuid);
    }

    @Override
    @RedisCacheable(
            key = "'driver:all:' + #email",
            ttl = 900,
            unit = TimeUnit.SECONDS
    )
    public List<Driver> getAll() {
        return List.of(driverRepository.findAll().toArray(new Driver[0]));
    }

    @RedisCacheable(
            key = "'driver:status:' + #driverId",
            ttl = 900,
            unit = TimeUnit.SECONDS
    )
    public DriverStatus driverStatus(UUID driverId){
        Driver driver = this.getById(driverId);
        return driver.getStatus();
    }


}
