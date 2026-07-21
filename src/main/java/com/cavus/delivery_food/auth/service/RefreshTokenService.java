package com.cavus.delivery_food.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cavus.delivery_food.auth.dto.RefreshTokenResponse;
import com.cavus.delivery_food.auth.entity.RefreshToken;
import com.cavus.delivery_food.auth.entity.User;
import com.cavus.delivery_food.auth.exceptions.ExpiredRefreshTokenException;
import com.cavus.delivery_food.auth.exceptions.InvalidRefreshTokenException;
import com.cavus.delivery_food.auth.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;


    @Transactional
    public String createRefreshToken(User user) {

        String rawToken = UUID.randomUUID().toString();

        RefreshToken refreshToken = refreshTokenRepository.findByUser(user).orElseGet(RefreshToken::new);

        refreshToken.setTokenHash(hashToken(rawToken));

        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000));
        refreshToken.setRevoked(false);
        refreshToken.setUser(user);

        refreshTokenRepository.save(refreshToken);
        return rawToken;

    }
    
       /**
     * Client'tan gelen ham token'ı doğrular, ilişkili User'ı döner.
     */
    public User validateAndGetUser(String rawToken) {
        RefreshToken refreshToken = findValidToken(rawToken);
        return refreshToken.getUser();
    }

    /**
     * Token rotation: aynı satırda hash ve süreyi günceller (yeni satır oluşturmaz).
     */
    @Transactional
    public String rotateRefreshToken(String rawToken) {
        RefreshToken existing = findValidToken(rawToken);

        String newRawToken = UUID.randomUUID().toString();
        existing.setTokenHash(hashToken(newRawToken));
        existing.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000));
        existing.setRevoked(false);

        refreshTokenRepository.save(existing);
        return newRawToken;
    }

    @Transactional
    public void revokeRefreshToken(String rawToken) {
        String tokenHash = hashToken(rawToken);

        refreshTokenRepository
                .findByTokenHashAndRevokedFalse(tokenHash)
                .ifPresent(token -> token.setRevoked(true));
    }

    @Transactional
    public void revokeByUserId(UUID userId) {
        refreshTokenRepository.findByUserId(userId)
                .ifPresent(token -> token.setRevoked(true));
    }

    private RefreshToken findValidToken(String rawToken) {
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHashAndRevokedFalse(tokenHash)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ExpiredRefreshTokenException();
        }

        return refreshToken;
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 kullanılamıyor", e);
        }
    }


    
}
