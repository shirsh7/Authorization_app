package com.Oauth.jwt.Auth_app.dtos;

//iske andr login krne k liye jo email password chahiye vo aajaega
public record LoginRequest(
        String email,
        String password
) {
}
