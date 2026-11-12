package com.Oauth.jwt.Auth_app.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Entity
@Table(name = "roles")
public class Role {
    @Id //(name="role_id")
    private UUID id= UUID.randomUUID();
    @Column(unique = true, nullable = false)
    private String name;
}
