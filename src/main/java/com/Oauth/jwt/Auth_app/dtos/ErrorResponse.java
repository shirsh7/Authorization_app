package com.Oauth.jwt.Auth_app.dtos;

import org.springframework.http.HttpStatus;

public record ErrorResponse(String msg, HttpStatus status){//, String error) {

}
