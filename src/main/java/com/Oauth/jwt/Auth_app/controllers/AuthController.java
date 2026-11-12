package com.Oauth.jwt.Auth_app.controllers;

import com.Oauth.jwt.Auth_app.Security.CookieService;
import com.Oauth.jwt.Auth_app.Security.JwtService;
import com.Oauth.jwt.Auth_app.dtos.LoginRequest;
import com.Oauth.jwt.Auth_app.dtos.RefreshTokenRequest;
import com.Oauth.jwt.Auth_app.dtos.TokenResponse;
import com.Oauth.jwt.Auth_app.dtos.UserDto;
import com.Oauth.jwt.Auth_app.entites.RefreshToken;
import com.Oauth.jwt.Auth_app.entites.User;
import com.Oauth.jwt.Auth_app.repo.RefreshTokenRepo;
import com.Oauth.jwt.Auth_app.repo.UserRepo;
import com.Oauth.jwt.Auth_app.service.AuthService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepo;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;
    private final RefreshTokenRepo refreshTokenRepo;
    private final CookieService cookieService;

    @PostMapping("/login")
//@RequestBody LoginRequest loginRequest:-Tells Spring to read the incoming JSON request body and convert it into a Java LoginRequest object.
    //basically java mapping is done to that the credentials could be used for processing further
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response){
//first we will authenticate the user
       Authentication authenticate=authenticate(loginRequest);
        User user=userRepo.findByEmail(loginRequest.email()).orElseThrow(()->new BadCredentialsException("Invalid username or password"));
        if(!user.isEnable()){
            throw new DisabledException("user is disabled");
        }
        //generating refresh token
        String jti= UUID.randomUUID().toString();
        //object generation to save in db
        var refreshTokenob= RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .revoked(false)
                .build();
//refresh token info saved to db
refreshTokenRepo.save(refreshTokenob);
        //generate the jwt token
            String accessToken=jwtService.generateAccessToken(user);
            String refreshToken=jwtService.generateRefreshToken(user,refreshTokenob.getJti());//generating refresh token

        //use cookie service to attach refresh token
        cookieService.attachRefreshCookie(response,refreshToken,(int)jwtService.getRefreshTtlSeconds());
        cookieService.addNoStoreHeader(response);

        TokenResponse tokenResponse= TokenResponse.of(accessToken,refreshToken,jwtService.getAccessTtlSeconds(),modelMapper.map(user,UserDto.class));
        return ResponseEntity.ok(tokenResponse);
    }
    //checks whether the email and password provided by the user are valid using Spring Security
    private Authentication authenticate(LoginRequest loginRequest){
    try{
         return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(),loginRequest.password()));
    } catch (Exception e) {
        throw new BadCredentialsException("Invalid credentials!! username or password is invalid");
    }
    }
//access and refresh token renew api
   @PostMapping("/refresh")
   //RefreshTokenRequest body-Optionally receives a refresh token from the JSON request body
   public ResponseEntity<TokenResponse> refreshToken(
           @RequestBody(required = false) RefreshTokenRequest body,
           HttpServletResponse response,
           HttpServletRequest request
   ){

       //this method will read the token either from cookie(request) or from the the request(body)
    String refreshToken=readRefreshTokenFromRequest(body,request).orElseThrow(()->new BadCredentialsException("refresh token missing"));
   if(!jwtService.isRefreshToken(refreshToken)){
       throw new BadCredentialsException("Inavalid refresh token type");
   }
   String jti=jwtService.getJti(refreshToken);//original one
   UUID userId=jwtService.getUserId(refreshToken);

   //fetching stored refresh token
       RefreshToken storedRefreshToken=refreshTokenRepo.findByJti(jti).orElseThrow(()->new BadCredentialsException("Refresh token not recognized"));
       if(storedRefreshToken.isRevoked()){
           throw new BadCredentialsException("the refresh token is revoked");

       }
       if(storedRefreshToken.getExpiresAt().isBefore(Instant.now())){
           throw new BadCredentialsException("refresh token expired");
       }
       if(!storedRefreshToken.getUser().getId().equals(userId)){
           throw new BadCredentialsException("Refresh toekn doesnt belong to this user");
       }
       //rotating refresh token
       storedRefreshToken.setRevoked(true);
       String newJti=UUID.randomUUID().toString();
       storedRefreshToken.setReplacedByToken(newJti);
       refreshTokenRepo.save(storedRefreshToken);

       User user=storedRefreshToken.getUser();

       //building a new refresh token
       var newRefreshtokenOb=RefreshToken.builder()
               .jti(newJti)
               .user(user)
               .createdAt(Instant.now())
               .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
               .revoked(false)
               .build();
       refreshTokenRepo.save(newRefreshtokenOb);
       String newAccessToken= jwtService.generateAccessToken(user);
       String newRefreshToken=jwtService.generateRefreshToken(user,newRefreshtokenOb.getJti());

       cookieService.attachRefreshCookie(response,newRefreshToken,(int)jwtService.getRefreshTtlSeconds());
       cookieService.addNoStoreHeader(response);
       return ResponseEntity.ok(TokenResponse.of(newAccessToken,newRefreshToken,jwtService.getAccessTtlSeconds(),modelMapper.map(user,UserDto.class)));
    }

    @PostMapping("/logout")

public ResponseEntity<Void> logout(HttpServletRequest request,HttpServletResponse response) {
        readRefreshTokenFromRequest(null, request).ifPresent(token -> {
            try {
                if (jwtService.isRefreshToken(token)) {
                    String jti = jwtService.getJti(token);
                    refreshTokenRepo.findByJti(jti).ifPresent(rt -> {
                        rt.setRevoked(true);
                        refreshTokenRepo.save(rt);

                    });
                }
            } catch (JwtException ignored) {

            }
        });
        //removing cookies as well upon logout
        cookieService.clearRefreshCookie(response);
        cookieService.addNoStoreHeader(response);
        SecurityContextHolder.clearContext();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }




    //this method(readRefreshTokenFromRequest) will read token from the request header or request body
    //If it finds a refresh token, you get the token as a String
    private Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request) {
        //1.prefer reading refresh token form cookie Read the token from the cookie
        if(request.getCookies()!=null){//Check whether cookies exist
           Optional<String> fromCookie= Arrays.stream(request.getCookies())//Stream through the cookies eg Suppose the request contains three cookies: JSESSIONID ,theme ,refreshToken The stream allows you to process them one by one
                    .filter(c->cookieService.getRefreshTokenCookieName().equals(c.getName()))//This keeps only the cookie whose name matches the configured name, such as refreshToken.
                    .map(Cookie::getValue)//extracting the value of the cookie ie refresh token
                    .filter(v->!v.isBlank())//checking if the value is null or not
                    .findFirst();

           if(fromCookie.isPresent()){
               return fromCookie;
           }
        }

        //request body me aara token to
        if(body!=null&& !body.refreshToken().isBlank()){
            return Optional.of(body.refreshToken());
        }
        // 3. if the token is sent in Custom header
        String refreshHeader = request.getHeader("X-Refresh-Token");
        if (refreshHeader != null && !refreshHeader.isBlank()) {
            return Optional.of(refreshHeader.trim());
        }

// if the token is sent in Authorization = Bearer <token> just like the access token
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String candidate = authHeader.substring(7).trim();
            if (!candidate.isEmpty()) {
                try {
                    if (jwtService.isRefreshToken(candidate)) {
                        return Optional.of(candidate);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return Optional.empty();

    }


    @PostMapping("/register")           //Spring Boot reads the JSON from the HTTP request body and converts it into a Java UserDto object.so you need to pass userdto in parameter in the req
    //ou need to send JSON data that matches the fields expected by your UserDto when calling the registration API.
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDto));
    }
}
