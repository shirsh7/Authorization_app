package com.Oauth.jwt.Auth_app.service;

import com.Oauth.jwt.Auth_app.dtos.UserDto;

import java.util.UUID;

public interface UserService {
UserDto createUser(UserDto userDto);


UserDto getUserByEmail(String email);


UserDto updateUser(UserDto userDto,String userId);


void deleteUser(String userId);

UserDto getUserById(String userId);

Iterable<UserDto> getAllUser();

public UserDto promoteUser(UUID userid);
}
