package com.Oauth.jwt.Auth_app.entites;

//import java.security.Provider;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Entity
@Table(name="users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")//in java we will refer it by id but in table its user_id
    private UUID id;//class that represents unique ids
    @Column(name = "user_name", unique = true)//no two persons can have same email
    private String email;
    private String name;
    private String password;
    private String image;
    private boolean enable=true;
    private Instant createdAt=Instant.now();//it is a class that shows date and time in utc format when the user is created
    private Instant updatedAt=Instant.now();
    @Enumerated(EnumType.STRING)
    private Provider provider=Provider.LOCAL;//the users might sign in from different platforms like gmail,github,etc if no provider is passed it will be marked as default
    private String providerId;

    //here another table will be created using join
    @Builder.Default
    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name="user_roles",joinColumns = @JoinColumn(name="user_id"),inverseJoinColumns = @JoinColumn(name="role_id"))
    private Set<Role> roles= new HashSet<>();

    @PrePersist//when ever an object is created or saved this method will run
    protected void onCreate(){
       Instant now=Instant.now();
       if(createdAt==null){
           createdAt=now;
       }else{
           updatedAt=now;
       }
    }
    @PreUpdate
    protected void onUpdate(){
        updatedAt=Instant.now();
    }

    //this method will return the roles a user have
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //SimpleGrantedAuthority is a implementation class of getAuthority class
      /*  List<SimpleGrantedAuthority> authorities=roles.stream().map(role->new SimpleGrantedAuthority(role.getName())).toList();
        return authorities;*/
        /*if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }*/

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();

    }

    @Override
    public String getUsername() {
        return this.email;// we are considering email as out username
    }

    @Override
    public boolean isEnabled() {
        return this.enable;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isAccountNonExpired() {
        //Call the default implementation of isAccountNonExpired() provided by the UserDetails interface.ie true
       // return UserDetails.super.isAccountNonExpired();
        return true;
    }
}
