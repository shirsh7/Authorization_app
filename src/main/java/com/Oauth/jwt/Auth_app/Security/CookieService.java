package com.Oauth.jwt.Auth_app.Security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@Getter
public class CookieService {
    private final String refreshTokenCookieName;
    private final boolean cookieHttpOnly;
    private final boolean cookieSecure;
    private final String cookieDomain;
    private final String cookieSameSite;

    public CookieService(
            @Value("${security.jwt.refresh-token-cookie-name}") String refreshTokenCookieName,
            @Value("${security.jwt.cookie-http-only}")boolean cookieHttpOnly,
            @Value("${security.jwt.cookie-secure}")boolean cookieSecure,

            @Value("${security.jwt.cookie-same-site}")String cookieSameSite,
            @Value("${security.jwt.cookie-domain}")String cookieDomain) {
        this.refreshTokenCookieName = refreshTokenCookieName;
        this.cookieHttpOnly = cookieHttpOnly;
        this.cookieSecure = cookieSecure;
        this.cookieDomain = cookieDomain;
        this.cookieSameSite = cookieSameSite;
    }
    //expose the cookie name so that controller can read the same cookie consistently
   /* public String getRefreshTokenCookieName(){
        return refreshTokenCookieName;
    }*/

    //You're configuring a cookie using Spring's existing ResponseCookie class
    //method to attach cookie to response
    //value is refresh token ,by response we will attach it , maxage is cookie life
    //ResponseCookie is a class provided by the Spring Framework.Creates a cookie builder using the cookie name and value
    public void attachRefreshCookie(HttpServletResponse response,String value,int maxAge){
        //This starts building a cookie using its name and value.
        var responseCookieBuilder=ResponseCookie.from(refreshTokenCookieName,value)
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .path("/")//domain ki kis path pr cookie aajae
                .sameSite(cookieSameSite);

        if(cookieDomain!=null && !cookieDomain.isBlank()){
            responseCookieBuilder.domain(cookieDomain);
        }
        ResponseCookie responseCookie=responseCookieBuilder.build();//This creates the configured cookie object
        response.addHeader(HttpHeaders.SET_COOKIE,responseCookie.toString());//This adds the Set-Cookie header to the HTTP response. The browser can then store the cookie.response.addHeader() sends it to the browser

    }
    //clearing the refreshtoken
    public void clearRefreshCookie(HttpServletResponse response){
        var builder=ResponseCookie.from(refreshTokenCookieName,"")
                .maxAge(0)
                .path("/")
                .sameSite(cookieSameSite)
                .secure(cookieSecure)
                .httpOnly(cookieHttpOnly);

        if(cookieDomain!=null && !cookieDomain.isBlank()){
            builder.domain(cookieDomain);
        }
        ResponseCookie responseCookie=builder.build();//This creates the configured cookie object
        response.addHeader(HttpHeaders.SET_COOKIE,responseCookie.toString());
    }
    //These headers tell caches not to store the response.
    public void addNoStoreHeader(HttpServletResponse response){
        response.setHeader(HttpHeaders.CACHE_CONTROL,"no-store");
        response.setHeader("Pragma","no-cache");
    }
}
