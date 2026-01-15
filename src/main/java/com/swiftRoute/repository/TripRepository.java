package com.swiftRoute.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swiftRoute.entity.Trip;

@Repository
public interface TripRepository extends JpaRepository<Trip,UUID> {

}
