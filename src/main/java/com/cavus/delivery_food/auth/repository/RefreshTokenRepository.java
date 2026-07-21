package com.cavus.delivery_food.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cavus.delivery_food.auth.entity.RefreshToken;
import com.cavus.delivery_food.auth.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);

    Optional<RefreshToken> findByUser(User user);

    Optional<RefreshToken> findByUserId(UUID userId);
    
}
