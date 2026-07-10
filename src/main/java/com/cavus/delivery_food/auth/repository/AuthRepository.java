package com.cavus.delivery_food.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cavus.delivery_food.auth.entity.User;

public interface AuthRepository extends JpaRepository<User, UUID> {
    

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
