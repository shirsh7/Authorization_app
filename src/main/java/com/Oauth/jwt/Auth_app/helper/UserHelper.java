package com.Oauth.jwt.Auth_app.helper;

import java.util.UUID;

public class UserHelper {
    public static UUID parseUUID(String userid){
        return UUID.fromString(userid);
    }
}
