package com.Oauth.jwt.Auth_app.dtos;

import com.Oauth.jwt.Auth_app.entites.Provider;
import com.Oauth.jwt.Auth_app.entites.Role;
import com.Oauth.jwt.Auth_app.entites.User;
import jakarta.persistence.*;
import lombok.*;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    private UUID id;//id coming from db
    private String email;
    private String name;
    private String password;
    private String image;
    private boolean enable=true;
    private Instant createdAt=Instant.now();//it is a class that shows date and time in utc format when the user is created
    private Instant updatedAt=Instant.now();
    private Provider provider=Provider.LOCAL;//the users might sign in from different platforms like gmail,github,etc if no provider is passed it will be marked as default


    private Set<RoleDto> roles= new HashSet<>();

}
