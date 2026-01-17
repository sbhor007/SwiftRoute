package com.swiftRoute.repository;

import java.util.Optional;
import java.util.UUID;

import com.swiftRoute.enums.UserRole;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.swiftRoute.entity.User;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailForAuth(@Param("email") String email);
    Optional<Boolean> existsByEmailAndRole(String email, UserRole role);
}
