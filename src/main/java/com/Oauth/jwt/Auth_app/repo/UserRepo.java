package com.Oauth.jwt.Auth_app.repo;

import com.Oauth.jwt.Auth_app.entites.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
//PROVIDE ENTITY NAME AND ID TYPE(PK)
public interface UserRepo extends JpaRepository<User, UUID> {
//Spring Data JPA automatically creates an implementation/proxy of UserRepo and registers it as a Spring bean.
    //custom query to find by email id in this also you have to follow naming convention like here findBy
    Optional<User> findByEmail(String email);
    //whether user exist in db or not
    boolean existsByEmail(String email);
}
