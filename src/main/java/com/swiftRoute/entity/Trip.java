package com.swiftRoute.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.swiftRoute.enums.TripStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "trips",
        indexes = {
                @Index(name = "idx_trip_driver", columnList = "driver_id"),
                @Index(name = "idx_trip_status", columnList = "status")
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalDistanceKm;

    private Integer totalDurationMin;

    @Column(precision = 10, scale = 2)
    private BigDecimal fareAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStatus status;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = TripStatus.CREATED;
    }
}
