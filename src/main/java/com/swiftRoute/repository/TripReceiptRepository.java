package com.swiftRoute.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swiftRoute.entity.TripReceipt;

@Repository
public interface TripReceiptRepository extends JpaRepository<TripReceipt,UUID>{

}
