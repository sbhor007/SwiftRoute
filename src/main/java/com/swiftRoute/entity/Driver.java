package com.swiftRoute.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.swiftRoute.enums.DriverStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "drivers",
        indexes = {
                @Index(name = "idx_driver_code", columnList = "driver_code"),
                @Index(name = "idx_driver_status", columnList = "status")
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "driver_code", nullable = false, unique = true)
    private String driverCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriverStatus status;

    @Column(nullable = false)
    private Boolean isAvailable;

    @Column(nullable = false)
    private Boolean isVerified;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_vehicle_id", unique = true)
    private Vehicle assignedVehicle;

    @OneToMany(mappedBy = "driver", fetch = FetchType.LAZY)
    private List<Trip> trips;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        if (isAvailable == null) isAvailable = false;
        if (isVerified == null) isVerified = false;
    }
}
