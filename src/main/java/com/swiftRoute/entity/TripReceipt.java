package com.swiftRoute.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trip_receipts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TripReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false, unique = true)
    private Trip trip;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal baseFare;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal distanceFare;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal timeFare;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal tax;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal total;

    private LocalDateTime generatedAt;

    @PrePersist
    void onCreate() {
        generatedAt = LocalDateTime.now();
    }
}