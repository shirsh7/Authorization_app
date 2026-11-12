package com.Oauth.jwt.Auth_app.entites;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "refresh_tokens",indexes={//we are creating index here because there will be frequent calls to the db so to make those calls faster we use index
@Index(name="refresh_tokens_jti_idx",columnList="jti", unique=true),//@Index creates database indexes to help certain queries run faster
@Index(name="refresh_tokens_user_id_idx",columnList="user_id")
})
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="jti", unique = true,nullable = false,updatable = false)
    private String jti;//the refresh token's id

    //fetch = FetchType.LAZY
    //This tells JPA that it should generally delay loading the full User object until it is accessed, rather than fetching it eagerly every time the refresh-token record is loaded
    @ManyToOne(optional = false,fetch = FetchType.LAZY)
    @JoinColumn(name="user_id",nullable = false,updatable = false)//@JoinColumn identifies the foreign-key column
    private User user;//one user can generate many tokens

    @Column(updatable = false,nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    private String replacedByToken;//the new refreshtoken


}
