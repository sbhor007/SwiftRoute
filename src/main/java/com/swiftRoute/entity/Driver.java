package com.swiftRoute.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.swiftRoute.enums.DriverStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "drivers")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Driver {
	@Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

	@Column(name = "driver_code", unique = true, nullable = false)
    private String driverCode;

    private String name;
    private String phone;
    private String vehicleNumber;

    @Enumerated(EnumType.STRING)
    private DriverStatus status;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
