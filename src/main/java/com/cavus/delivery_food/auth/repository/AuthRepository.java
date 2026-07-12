package com.cavus.delivery_food.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.cavus.delivery_food.auth.entity.User;

public interface AuthRepository extends JpaRepository<User, UUID> {
    

    // ! EntityGraph ile roles ve roles.permissions'i birlikte getirir.
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
