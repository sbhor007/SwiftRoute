package com.swiftRoute.repository;

import java.util.Optional;
import java.util.UUID;

import com.swiftRoute.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swiftRoute.entity.User;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
    Optional<Boolean> existsByEmailAndRole(String email, UserRole role);
}
