package com.Oauth.jwt.Auth_app.Security;

import com.Oauth.jwt.Auth_app.entites.User;
import com.Oauth.jwt.Auth_app.helper.UserHelper;
import com.Oauth.jwt.Auth_app.repo.UserRepo;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component//@Component tells Spring to discover this class during component scanning and register an instance as a bean in the application context
@RequiredArgsConstructor
//Java inheritance means your class inherits behavior from OncePerRequestFilter and can override its methods.
//onceperrequestfilter is a class extending genericfilterbean class and genericfilterbean class is implementing filter interface
//onceperrequestfilter class provides simple base implementation of filters
//a Spring base class for implementing filters intended to execute once per request dispatch.
//Java inheritance means your class inherits behavior from OncePerRequestFilter and can override its methods.
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepo userRepo;

    @Override
    //this method will always be invoked once before any request
    //It passes two objects forward:
    //request — contains information about the incoming request, such as its URL, headers and body.
    //response — represents the HTTP response that will eventually be sent back to the client.
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header=request.getHeader("Authorization");
        if(header!=null&&header.startsWith("Bearer ")){
            //token extract and validate it then authorization the set it under security context
            String token=header.substring(7);

            try{

                //check if the token is access token or refresh token basically try catch will only work if the token is access token
                if(!jwtService.isAccessToken(token)){
                    filterChain.doFilter(request,response);
                    return;
                }

                Jws<Claims> parse= jwtService.parse(token);
                Claims payload=parse.getPayload();
                String userid=payload.getSubject();
                UUID userUuid= UserHelper.parseUUID(userid);//since throughout the project we are using UUID format and also by using this we can fetch the user
                userRepo.findById(userUuid)
                        .ifPresent(user ->{
                            //check if the user is enabled or not if it is enabled then only further processing will be done
                            if(user.isEnable()){
                                //we got the user from db now we are going to set the user for authentication but before that we are going to extract some infos
                                //if role is empty then empty list will be returned else roles will be taken out one by one and will be converted in to simplegrantedauthority
                                List<GrantedAuthority> authorities= user.getRoles()==null ? List.of():user.getRoles().stream()
                                        .map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());

                                //making authenticaiton and to make it we will use authentication class interface implementation class
                                //UsernamePasswordAuthenticationToken extends abstractauthenticationtoken class and this class is implementing authentication
                                //create an Authentication object.
                                //This object contains:
                                //user — the authenticated user's principal (identity).
                                //null — credentials, which aren't needed here because the JWT has already been validated.
                                //authorities — the user's roles or permissions, such as ROLE_ADMIN.
                                UsernamePasswordAuthenticationToken authentication= new UsernamePasswordAuthenticationToken(
                                        user.getEmail(),
                                        null,
                                        authorities

                                );
                                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                //first we will check if the securitycontextholder is empty or not then take out the context then call the authentication and pass our authentication here
                                //store that object in the SecurityContext.
                                if(SecurityContextHolder.getContext().getAuthentication()==null){
                                    SecurityContextHolder.getContext().setAuthentication(authentication);
                                }
                            }


                        } );



            } catch (ExpiredJwtException e) {
                request.setAttribute("error","Token Expired");
               // e.printStackTrace();

            } catch (Exception e){
                request.setAttribute("error","invalid token");
                //e.printStackTrace();
            }
        }
        //It passes two objects forward:
        //request — contains information about the incoming request, such as its URL, headers and body.
        //response — represents the HTTP response that will eventually be sent back to the client.
        filterChain.doFilter(request,response);//moving the request to next filter
    }
    //when ever a req comes with this api "/api/v1/auth" then dont run this filter class
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getRequestURI().startsWith("/api/v1/auth");
    }
}
