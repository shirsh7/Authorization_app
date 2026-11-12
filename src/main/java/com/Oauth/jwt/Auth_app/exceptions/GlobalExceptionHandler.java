package com.Oauth.jwt.Auth_app.exceptions;

import com.Oauth.jwt.Auth_app.dtos.ApiError;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.Oauth.jwt.Auth_app.dtos.ErrorResponse;

import java.rmi.ServerError;
import org.slf4j.Logger;




//Whenever my application throws ResourceNotFoundException, I'll decide what HTTP response should be sent to the client

//catches that exception globally and converts it into an HTTP response and one more thing this will only work if the exception comes from servlet level
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Logger logger= LoggerFactory.getLogger(GlobalExceptionHandler.class);

//you can either create multiple methods for multiple exceptions or create a generic one for all

    @ExceptionHandler({
            UsernameNotFoundException.class,
            BadCredentialsException.class,
            CredentialsExpiredException.class,
            DisabledException.class
    })
    public ResponseEntity<ApiError> handleAuthException(Exception e, HttpServletRequest request){
        logger.info("Exception : {}",e.getClass().getName());
        var apiError=ApiError.of(HttpStatus.BAD_REQUEST.value(),"Bad_Request",e.getMessage(),request.getRequestURI());
        return ResponseEntity.badRequest().body(apiError);
    }

    //resource not found exception handling method
    @ExceptionHandler(ResourceNotFoundException.class)//you can specify here multiple classes and this method will only run for those classes only
    ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException execp){
        ErrorResponse internalServerError =new ErrorResponse(execp.getMessage(), HttpStatus.NOT_FOUND);//,"internal server error");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(internalServerError);//returning the variable with status
    }
    @ExceptionHandler(IllegalArgumentException.class)//you can specify here multiple classes and this method will only run for those classes only
    ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException execp){
        ErrorResponse internalServerError =new ErrorResponse(execp.getMessage(), HttpStatus.BAD_REQUEST);//,"internal server error");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(internalServerError);//returning the variable with status
    }

}
