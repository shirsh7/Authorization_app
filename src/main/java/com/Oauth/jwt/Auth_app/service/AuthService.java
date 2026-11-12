package com.Oauth.jwt.Auth_app.service;

import com.Oauth.jwt.Auth_app.dtos.UserDto;

public interface AuthService {
    UserDto registerUser(UserDto userDto);

    //UserDto loginUser(UserDto userDto);
}
