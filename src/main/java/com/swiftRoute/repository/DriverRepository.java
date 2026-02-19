package com.swiftRoute.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swiftRoute.entity.Driver;

@Repository
public interface DriverRepository extends JpaRepository<Driver,UUID> {
    Optional<Driver> findByDriverCode(String driverCode);

    Optional<Driver> findByUserId(UUID userId);

    Optional<Driver> findDriverByUserEmail(String userEmail);
}
