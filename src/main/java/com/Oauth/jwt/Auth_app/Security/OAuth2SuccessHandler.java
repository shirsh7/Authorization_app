package com.Oauth.jwt.Auth_app.Security;

import com.Oauth.jwt.Auth_app.entites.Provider;
import com.Oauth.jwt.Auth_app.entites.RefreshToken;
import com.Oauth.jwt.Auth_app.entites.User;
import com.Oauth.jwt.Auth_app.repo.RefreshTokenRepo;
import com.Oauth.jwt.Auth_app.repo.UserRepo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
//import java.util.logging.Logger;
import org.slf4j.Logger;

//This method is called by Spring Security's success handler when authentication succeeds.
@Component
@AllArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());
    private final UserRepo userRepo;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final RefreshTokenRepo refreshTokenRepo;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
    logger.info("Successful authentication");
    logger.info(authentication.toString());//prints information about the Authentication object, which include the principal and authorities
//OAuth2User is an interface provided by Spring Security
//Spring Security creates or provides a concrete implementation of that interface during OAuth2 login.
//OOAuth2User is an interface provided by Spring SecurityAuth2User represents the user information that Spring Security has obtained from Google after a successful OAuth2 login.
    OAuth2User oAuth2User=(OAuth2User)authentication.getPrincipal();
    //identifying the user
        String registrationId="unknown";
        //Its purpose is to identify which OAuth2 provider the user logged in with, such as Google.
        //OAuth2AuthenticationToken is a class provided by Spring Security that represents a user who has successfully authenticated through an OAuth2 login, such as Google login.
        if(authentication instanceof OAuth2AuthenticationToken token){
            registrationId=token.getAuthorizedClientRegistrationId();

        }
        logger.info("registrationid:"+registrationId);
        logger.info("user:"+oAuth2User.getAttributes().toString());

        User user;
        switch (registrationId) {
            case "google" -> {
                String googleId = oAuth2User.getAttributes().getOrDefault("sub", "").toString();//extracting the subject(google id)
                String email = oAuth2User.getAttributes().getOrDefault("email", "").toString();
                String name = oAuth2User.getAttributes().getOrDefault("name", "").toString();
                String picture = oAuth2User.getAttributes().getOrDefault("picture", "").toString();

                /*user = User.builder()
                        .email(email)
                        .name(name)
                        .image(picture)
                        .provider(Provider.GOOGLE)
                        .build();
                userRepo.findByEmail(email).ifPresentOrElse(user1 -> {
                    logger.info("user already there in database");
                    //logger.info(user1.toString());
                }, () -> {//else part if user doesnt exist
                    userRepo.save(user);
                });*/
                User googleUser = User.builder()
                        .email(email)
                        .name(name)
                        .image(picture)
                        .enable(true)
                        .provider(Provider.GOOGLE)
                        .providerId(googleId)
                        .build();
                //if the user already exist then that is return if not first that user is saved then returned
                user = userRepo.findByEmail(email)
                        .orElseGet(() -> userRepo.save(googleUser));
            }
            case "github"->{
                String name=oAuth2User.getAttributes().getOrDefault("login","").toString();
                String email=(String) oAuth2User.getAttributes().get("email");
                if(email==null){
                    email=name + "@github.com";
                }
                String githubid=oAuth2User.getAttributes().getOrDefault("id","").toString();
                String image=oAuth2User.getAttributes().getOrDefault("avatar_url","").toString();
                User githubUser = User.builder()
                       .email(email)
                        .name(name)
                        .image(image)
                        .enable(true)
                        .provider(Provider.GITHUB)
                        .providerId(githubid)
                        .build();
                user= userRepo.findByEmail(email).orElseGet(()->userRepo.save(githubUser));
            }
            //logger.info("GitHub access token: {}", accessToken);
            default ->  {
                throw new RuntimeException("Invalid registration id");
            }
        }
        //sending refresh token rather than sending access token then the user can send and get access token
        String jti=UUID.randomUUID().toString();
        RefreshToken refreshTokenobj=RefreshToken.builder()
                .jti(jti)
                .user(user)
                .revoked(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .build();

        refreshTokenRepo.save(refreshTokenobj);
        String accessToken= jwtService.generateAccessToken(user);
        String refreshToken= jwtService.generateRefreshToken(user,refreshTokenobj.getJti());
        //attaching the refresh token to a cookie for sending
        cookieService.attachRefreshCookie(response,refreshToken,(int)jwtService.getRefreshTtlSeconds());
        response.getWriter().write("Login successful");
    }
}
