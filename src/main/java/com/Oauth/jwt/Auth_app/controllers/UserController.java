package com.Oauth.jwt.Auth_app.controllers;

import com.Oauth.jwt.Auth_app.dtos.UserDto;
import com.Oauth.jwt.Auth_app.entites.User;
import com.Oauth.jwt.Auth_app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    //UserController is responsible for receiving the HTTP request, but it shouldn't contain the actual user/business logic itself. It delegates that work to UserService.
   //So conceptually Spring does: UserService userService = userServiceImplObject;
    //userService
    //    ↓
    //UserServiceImpl object
    //    ↓
    //createUser()
    //    ↓
    //actual implementation in UserServiceImpl runs
    private final UserService userService;

    @PostMapping
    @Transactional
//to convert the coming json data(userdto) to java mappings into userdto object we will use requestbody
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto){
        //ResponseEntity represents the HTTP response that you're sending back to the client
        //HTTP Response
        //
        //Status: 201 CREATED
        //
        //Body:
        //{
        //    "id": "123",
        //    "name": "John",
        //    "email": "john@gmail.com"
        //}
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDto));

    }

    //get all user
    @GetMapping
 public ResponseEntity<Iterable<UserDto>> getAlluser(){
        return ResponseEntity.ok(userService.getAllUser());
 }

 //find by emailid
 //find user by email here path variabkle is used because email should be dynamically assigned to the url for searching
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email){
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    //delete by id
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable String userId){
        userService.deleteUser(userId);
    }

    //update user
    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updatedUser(@RequestBody UserDto userDto,@PathVariable("userId")String userId){
        return ResponseEntity.ok(userService.updateUser(userDto,userId));
    }

    //get user by id
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String userId){
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/{userid}/promote")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserDto> promotedUser(@PathVariable UUID userid){
        UserDto user=userService.promoteUser(userid);
        return ResponseEntity.ok(user);
    }
}
//Controller
//    ↓
//UserService interface
//    ↓
//UserServiceImpl
//    ↓
//UserRepo
//    ↓
//Database