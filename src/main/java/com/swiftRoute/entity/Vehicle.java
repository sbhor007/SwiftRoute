package com.swiftRoute.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "vehicles",
        indexes = {
                @Index(name = "idx_vehicle_number", columnList = "vehicle_number")
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "vehicle_number", nullable = false, unique = true)
    private String vehicleNumber;

    private String model;

    private Integer capacity;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private Boolean isAssigned;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_driver_id")
    private Driver assignedDriver;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
        if (isAssigned == null) isAssigned = false;
    }
}
