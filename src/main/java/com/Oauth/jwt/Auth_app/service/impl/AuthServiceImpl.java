package com.Oauth.jwt.Auth_app.service.impl;

import com.Oauth.jwt.Auth_app.config.AppContants;
import com.Oauth.jwt.Auth_app.dtos.UserDto;
import com.Oauth.jwt.Auth_app.repo.RoleRepo;
import com.Oauth.jwt.Auth_app.service.AuthService;
import com.Oauth.jwt.Auth_app.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    //private final RoleRepo roleRepo;
    @Override
    //to register user we are using userDto
    public UserDto registerUser(UserDto userDto) {
        //before sending password for registration we are going to encrypt it
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));

        UserDto userDto1=userService.createUser(userDto);

        return userDto1;
    }
}
