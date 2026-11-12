package com.Oauth.jwt.Auth_app.exceptions;

public class ResourceNotFoundException extends RuntimeException{
    //creates a specific exception

    //if no values are passed in constructor it will goto default constructor and return error msg Resource not found
    public ResourceNotFoundException(String msg){
        super(msg);//passing the msg to parent class the runtimeexception class
    }

    //default error msg
    public ResourceNotFoundException(){
        super("Resource not found");//passing the msg to parent class the runtimeexception class
    }
}
