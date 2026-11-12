package com.Oauth.jwt.Auth_app.dtos;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType,
        UserDto user) {
//These five components represent the data stored in your response:
//Component	         Purpose
//accessToken	 JWT used to access protected APIs
//refreshToken	 Used to obtain a new access token
//expiresIn	     Access token's validity duration, typically in seconds
//tokenType	     Authentication scheme, such as Bearer
//user	         User information represented by UserDto


 //This is a factory method. It creates a TokenResponse object while automatically setting tokenType to "Bearer"
public static TokenResponse of(String accessToken,
                                   String refreshToken,
                                   long expiresIn,
                                   UserDto user){
 return new TokenResponse(accessToken, refreshToken, expiresIn, "Bearer", user);
}

}
