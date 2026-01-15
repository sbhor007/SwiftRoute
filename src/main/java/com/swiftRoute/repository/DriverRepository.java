package com.swiftRoute.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swiftRoute.entity.Driver;

@Repository
public interface DriverRepository extends JpaRepository<Driver,UUID> {

}
