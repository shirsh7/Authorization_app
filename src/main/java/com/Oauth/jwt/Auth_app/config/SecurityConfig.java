package com.Oauth.jwt.Auth_app.config;


import com.Oauth.jwt.Auth_app.Security.JwtAuthenticationFilter;
import com.Oauth.jwt.Auth_app.Security.OAuth2SuccessHandler;
import com.Oauth.jwt.Auth_app.dtos.ApiError;
import jakarta.servlet.http.HttpServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Configuration //Spring will create/manage a SecurityConfig object and look inside this class for methods marked with @Bean
@EnableMethodSecurity
public class SecurityConfig {

    //@Autowired
    private  JwtAuthenticationFilter jwtAuthenticationFilter;
    //private OAuth2SuccessHandler oAuth2SuccessHandler;
    private AuthenticationSuccessHandler successHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, AuthenticationSuccessHandler successHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.successHandler = successHandler;
    }

    /*
        @Bean
        public UserDetailsService user(){
            //UserBuilder is a builder object provided by Spring Security that helps you create a UserDetails object step by step.
            //UserBuilder is the tool used to construct the User
            //Encoding changes data into another format so it can be represented or transmitted.
            //"hello"
            //   ↓ Base64 encoding
            //"aGVsbG8="
            //the user class mentioned here implments UserDetails interface so it uses methods defined in interface to get the password of that User
            //so we wwant to use our custom user logic for it so we will implment the same UserDetails interfae with our user class to configure our custom user logic
            User.UserBuilder userBuilder=User.withDefaultPasswordEncoder();

            //UserDetails is a interface whose method it uses its prewritten class
            //Spring Security's UserDetails interface has this method:
           UserDetails user1= userBuilder.username("shirsh").password("abc").roles("ADMIN").build();
            UserDetails user2= userBuilder.username("priyansh").password("abcd").roles("ADMIN").build();

            return new InMemoryUserDetailsManager(user1,user2);
        }
    */
    //since every api is protected we need to make register ai public for registration so we are doing that here
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http.csrf(AbstractHttpConfigurer::disable)//if frontend and backend are configured on diff app then csrf needs to be disabled
                .cors(Customizer.withDefaults())
                .sessionManagement(sm->sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))// we dont want to save anything in the browser/server thats why we are making the session stateless
                .authorizeHttpRequests(authorizeHttpRequests ->
                    authorizeHttpRequests.requestMatchers("/api/v1/auth/register").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll()
                            .requestMatchers("/api/v1/auth/refresh").permitAll()
                            .requestMatchers("/api/v1/auth/logout").permitAll()
                            //all apis with get method are accessible to guest role
                            .requestMatchers(HttpMethod.GET).hasRole(AppContants.GUEST_ROLE)
                            //only admin role is allowed to access this api "/api/v1/users/**" till user or all user apis get apis are accessible to guest role
                            .requestMatchers("/api/v1/users/**").hasRole(AppContants.ADMIN_ROLE)
                        .anyRequest().authenticated()

                )
                .oauth2Login(oauth2 ->
                        //whenever the login is successful AuthenticationSuccessHandler class will be executed and that class will point it tothe implementation class OAuth2SuccessHandler class in java
                        oauth2.successHandler(successHandler)//when the login is successful uesr agar google se login kr leta hai to ye function chelga
                                .failureHandler(null)



                )
                .logout(AbstractHttpConfigurer::disable)//disabling logout as jwt will handle it




                //rest of the other apis need authentication then only it can be accessed
                //whenever any unauthenticated user who doesnt have permission tries to access protected apis this becomes the entry pnt for those kind of requests
                //study functional interface
                .exceptionHandling(ex->ex.authenticationEntryPoint((request, response, e) -> {
                    //error msg
                    //e.printStackTrace();
                    response.setStatus(401);
                    response.setContentType("application/json");//we are sending json format data back
                    String message=e.getMessage();//Spring Security passes an exception whose message is: Full authentication is required to access this resource this error is being shown when the user enters wrong jwt token
                    //to customize the error msg that is displayed when token is wrong or expired
                    String error=(String)request.getAttribute("error");//we are getting the error msg from jwtAuthenticationfilter class
                    if(error!=null){
                        message=error;
                    }

                   // Map<String,String>errorMap= Map.of("message",message,"statusCode", Integer.toString(401));
                    //now we are using the apierror record to send the error msg whenever a wrong token is entered
                    var apiError= ApiError.of(HttpStatus.UNAUTHORIZED.value(), "Unauthorized Access!!",message,request.getRequestURI(),true);
                    var objectMapper=new ObjectMapper();
                    response.getWriter().write(objectMapper.writeValueAsString(apiError));
                })              //configuring the role mismatch exception showing access denied
                                .accessDeniedHandler((request, response, e) ->{
                                            response.setStatus(403);
                                            response.setContentType("application/json");
                                            String message= e.getMessage();
                                            String error=(String)request.getAttribute("error");
                                            if(error!=null){
                                                message=error;
                                            }
                                            var apiError=ApiError.of(HttpStatus.FORBIDDEN.value(), "Forbidden Access",message,request.getRequestURI());

                                            var objectMapper=new ObjectMapper();
                                            response.getWriter().write(objectMapper.writeValueAsString(apiError));
                                        }


                                        )
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);//we are adding our filter jwtAuthenticationFilter before usernamepasswordauthenticationfilter runs work of usernamepasswordauthenticationfilter is to fetch the username and password
        return http.build();
    }


    @Bean//now spring security will use this only as default passwordencoder
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration){
        return configuration.getAuthenticationManager();
    }

}
