package com.Oauth.jwt.Auth_app.repo;

import com.Oauth.jwt.Auth_app.entites.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken>findByJti(String jti);
}
