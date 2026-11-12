package com.Oauth.jwt.Auth_app.Security;

import com.Oauth.jwt.Auth_app.entites.User;
import com.Oauth.jwt.Auth_app.exceptions.ResourceNotFoundException;
import com.Oauth.jwt.Auth_app.repo.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;//coming from spring framework
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor//@AllArgsConstructor creates a constructor using all fields (instance variables) declared in the class, regardless of whether they are dependencies.
//@RequiredArgsConstructor-it only creates construtor that are marked with final
//@AllArgsConstructor
//→ ALL fields
//@RequiredArgsConstructor
//→ final fields + @NonNull fields
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepo userRepo;

    @Override//prewritten method of userdetailservice class
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

       User user= userRepo.findByEmail(username).orElseThrow(()->new ResourceNotFoundException("Invalid username or password"));
        return user;
    }
}
