package com.Oauth.jwt.Auth_app.dtos;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

//You don't need to write this constructor yourself. Java generates it automatically.

//This record defines the data we want to return when an API request fails.
public record ApiError(int status,
                       String error,
                       String message,
                       String path,
                       OffsetDateTime timestamp) {

    //Why create the of() method? Instead of writing this every time you handle an error:\
    //ApiError apiError = new ApiError(
    //    404,
    //    "Not Found",
    //    "User not found",
    //    "/api/v1/users/123",
    //    OffsetDateTime.now(ZoneOffset.UTC)
    //);
    public static ApiError of(int status,String error,String message,String path){
        return new ApiError(status,error,message,path,OffsetDateTime.now(ZoneOffset.UTC));
    }

    public static ApiError of(int status,String error,String message,String path,boolean noDatetime){
        return new ApiError(status,error,message,path,null);
    }
}
