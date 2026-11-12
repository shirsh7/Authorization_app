package com.Oauth.jwt.Auth_app.Security;

import com.Oauth.jwt.Auth_app.entites.Role;
import com.Oauth.jwt.Auth_app.entites.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


//@Bean = "Spring, use this method's returned object as a bean."
@Service//Create and manage a bean/object of JwtService
@Getter
@Setter
public class JwtService {

    //final means they must be initialized once and then cannot be reassigned.
    private final SecretKey key;//This is the cryptographic key used to sign and verify JWTs.
    private final long accessTtlSeconds;//This stores how long the access token should be valid.
    private final long refreshTtlSeconds;//This stores how long the refresh token should be valid.
    private final String issuer;//This identifies who issued the JWT. This token was issued by this application/service

    //This constructor is called by Spring when it creates your JwtService bean.
    //here we used @value beacuse the values for it are already defined in application-dev.yaml file to it picks the value from there
    //Before creating this object, get these values from the Spring configuration."
    public JwtService(@Value("${security.jwt.secret}") String secret,
                      @Value("${security.jwt.access-ttl-seconds}") long accessTtlSeconds,
                      @Value("${security.jwt.refresh-ttl-seconds}")long refreshTtlSeconds,
                      @Value("${security.jwt.issuer}")String issuer) {
        if(secret==null||secret.length()<64){
            throw new IllegalArgumentException("invalid secret");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));//JWT signing needs a cryptographic SecretKey, not just a normal Java String
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
        this.issuer = issuer;//Take the value Spring gave me and store it inside this JwtService object."
    }

    //method for generate tokens
    //Take a User object → create a JWT containing that user's information → sign it → return the JWT as a String.
    public String generateAccessToken(User user) {
        //We need this because the JWT needs to know:When was it created? When should it expire?
        Instant now = Instant.now();//This gets the current date and time. like now = 2026-10-08T01:00:00Z
        List<String> roles=user.getRoles() ==null ? List.of(): user.getRoles().stream().map(Role::getName).toList();//here we take out the names of the roles and store it in the lsit eg [Role("USER"), Role("ADMIN")]
        //Start creating the JWT Jwts.builder() is a pre-written method from the JJWT library.It gives you a JWT builder so you can add information to the token.
        return Jwts.builder()
                .id(UUID.randomUUID().toString())//This creates a unique ID for this particular token
                .subject(user.getId().toString())//sub = Which user does this token belong to? we have passed id of the user here so id=token...
                .issuer(issuer)//who issued the token ie the application we have in yaml file
                .issuedAt(Date.from(now))//issuedAt means: When was this token created?Set when the token was created
                .expiration(Date.from(now.plusSeconds(accessTtlSeconds)))//You need to tell the JWT when it expires //current time + 3600 seconds= 1 hour later
                //These are custom pieces of information you're putting into the JWT.
                .claims(Map.of(
                        "email",user.getEmail(),
                        "roles",roles,
                        "typ","access"
                ))
                //signs the token and serializes the three parts into one string.
                .signWith(key, SignatureAlgorithm.HS512)//Sign this JWT using my secret key and the HS512 algorithm Because later,
                // when the client sends the JWT back to your server, your application can verify: "Was this token actually created by my application, or has someone modified it?"
                .compact();//It takes everything you've built:and produces the final JWT string: xxxxx.yyyyy.zzzzz
    }

    //method for generating refrsh token
    public String generateRefreshToken(User user,String jti) {
        Instant now = Instant.now();

        return Jwts.builder()
                .id(jti)
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTtlSeconds)))
                .claims(Map.of(
                        "email", user.getEmail(),
                        "typ", "refresh"
                ))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    //method to parse the token(to extract all the claims details) this will also check if the token is expired or not
    public Jws<Claims> parse(String token){
        try{
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        } catch (JwtException e) {
            throw e;
        }
    }

    //methid to check whether its a access token or a refreshed token
    public boolean isAccessToken(String token){
        Claims c=parse(token).getPayload();
        return "access".equals(c.get("typ"));
    }

    //methid to check whether its a access token or a refreshed token
    public boolean isRefreshToken(String token){
        Claims c=parse(token).getPayload();
        return "refresh".equals(c.get("typ"));
    }

    //to extract userid from the token
    public UUID getUserId(String token){
        Claims c=parse(token).getPayload();
        return UUID.fromString(c.getSubject());
    }

    //to extract token id
    public String getJti(String token){
        return parse(token).getPayload().getId();
    }
}

