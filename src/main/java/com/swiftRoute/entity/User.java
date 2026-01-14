package com.swiftRoute.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.swiftRoute.enums.UserRole;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
	 @Id
	    @GeneratedValue(strategy = GenerationType.UUID)
	    private UUID id;

	    @Column(nullable = false, length = 100)
	    private String name;

	    @Column(nullable = false, unique = true, length = 150)
	    private String email;

	    @Column(nullable = false)
	    private String password;

	    @Enumerated(EnumType.STRING)
	    @Column(nullable = false)
	    private UserRole role;

	    private LocalDateTime createdAt;
	    private LocalDateTime updatedAt;

	    @PrePersist
	    void onCreate() {
	        createdAt = LocalDateTime.now();
	    }

	    @PreUpdate
	    void onUpdate() {
	        updatedAt = LocalDateTime.now();
	    }
}
