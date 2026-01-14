package com.swiftRoute.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Vehicle {
	 @Id
	    @GeneratedValue(strategy = GenerationType.UUID)
	    private UUID id;

	    @Column(name = "vehicle_number", unique = true, nullable = false)
	    private String vehicleNumber;

	    private String model;
	    private Integer capacity;

	    private LocalDateTime createdAt;

	    @PrePersist
	    void onCreate() {
	        createdAt = LocalDateTime.now();
	    }

}
