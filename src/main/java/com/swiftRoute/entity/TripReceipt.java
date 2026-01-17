package com.swiftRoute.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal baseFare;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal distanceFare;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal timeFare;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tax;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(nullable = false)
    private String currency;

    private LocalDateTime generatedAt;

    @PrePersist
    void onCreate() {
        generatedAt = LocalDateTime.now();
        if (currency == null) currency = "INR";
    }
}
