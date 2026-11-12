package com.Oauth.jwt.Auth_app.service.impl;

import com.Oauth.jwt.Auth_app.config.AppContants;
import com.Oauth.jwt.Auth_app.dtos.UserDto;
import com.Oauth.jwt.Auth_app.entites.Provider;
import com.Oauth.jwt.Auth_app.entites.Role;
import com.Oauth.jwt.Auth_app.entites.User;
import com.Oauth.jwt.Auth_app.exceptions.ResourceNotFoundException;
import com.Oauth.jwt.Auth_app.helper.UserHelper;
import com.Oauth.jwt.Auth_app.repo.RoleRepo;
import com.Oauth.jwt.Auth_app.repo.UserRepo;
import com.Oauth.jwt.Auth_app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

//Because UserServiceImpl is marked with @Service, Spring needs to create an object (bean) of UserServiceImpl
@Service //Makes UserServiceImpl a Spring bean  Create and manage an object of UserServiceImpl
@RequiredArgsConstructor//automatically create paramterized cons automatically generates this constructor behind the scenes:
public class UserServiceImpl implements UserService {
//Constructor injection → puts the UserRepo bean inside UserServiceImpl.
private final UserRepo userRepo;//to perform anykind of user operation like saving user or fetching updating we nmeed user repo
private final ModelMapper modelMapper;
private final RoleRepo roleRepo;
    @Override
    public UserDto createUser(UserDto userDto) {

        if(userDto.getEmail()==null||userDto.getEmail().isBlank()){
            throw new IllegalArgumentException("Email is req");
        }
        if(userRepo.existsByEmail(userDto.getEmail())){
            throw new IllegalArgumentException("user already exist");
        }
        //now to save the dto to table we need to convert dto into entity first
        User user=modelMapper.map(userDto, User.class);

        System.out.println("Roles after mapping: " + user.getRoles());
        //user.setProvider(userDto.getProvider()!=null ? userDto.getProvider() : Provider.LOCAL);
        if (userDto.getProvider() != null) {
            user.setProvider(userDto.getProvider());
        } else {
            user.setProvider(Provider.LOCAL);
        }
        //assigning guest role by default
        Role role=roleRepo.findByName("ROLE_"+ AppContants.GUEST_ROLE).orElse(null);
        user.getRoles().add(role);
        User savedUser= userRepo.save(user);

        //converting savedUser to userdto
        return modelMapper.map(savedUser,UserDto.class);
    }
    /*public UserDto promoteUser(UUID userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User doesn't exist in database"));
        Role role = roleRepo.findByName("ROLE_" + AppContants.ADMIN_ROLE)
                .orElseThrow(() ->
                        new IllegalArgumentException("Admin role doesn't exist"));
        user.getRoles().add(role);
        User savedUser= userRepo.save(user);
        return modelMapper.map(savedUser,UserDto.class);
    }*/
    public UserDto promoteUser(UUID userId) {

        System.out.println("STEP 1: Finding user");

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User doesn't exist in database"));

        System.out.println("STEP 2: User found: " + user.getName());

        Role role = roleRepo.findByName("ROLE_" + AppContants.ADMIN_ROLE)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Admin role doesn't exist"));

        System.out.println("STEP 3: Admin role found");

        user.getRoles().add(role);

        System.out.println("STEP 4: Role assigned");

        User savedUser = userRepo.save(user);

        System.out.println("STEP 5: User saved");

        return modelMapper.map(savedUser, UserDto.class);
    }
    @Override
    public UserDto getUserByEmail(String email) {
        //1st way
      /*  if(userRepo.existsByEmail(email)){
            User user=userRepo.findByEmail(email).get();// error becoz findByEmail is optional<> so There may or may not be a User with this email and java wouldnt allow that so you need to add .get() then it will work because the first check already confirms the user is presnmt
        }else {
            throw new RuntimeException("user doesnt exist");
        }*/
        //2nd way
        User user = userRepo.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User doesn't exist here"));

        return modelMapper.map(user,UserDto.class);
    }

    @Override
    public UserDto updateUser(UserDto userDto, String userId) {
      //  User user=userRepo.findById(userId);
       // userRepo.
        UUID id= UserHelper.parseUUID(userId);
        User user=userRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("User with this id doesn't exist here"));
        //we are not going to change email id
        if(userDto.getName()!=null){
            user.setName(userDto.getName());
        }
        if(userDto.getImage()!=null){
            user.setImage(userDto.getImage());
        }
        if(userDto.getProvider()!=null){
            user.setProvider(userDto.getProvider());
        }
        if(userDto.getPassword()!=null){
            user.setPassword(userDto.getPassword());
        }
        user.setEnable(userDto.isEnable());
        user.setUpdatedAt(Instant.now());
        User updateduser=userRepo.save(user);
        return modelMapper.map(updateduser,UserDto.class);
    }

    @Override
    public void deleteUser(String userId) {
        UUID id= UserHelper.parseUUID(userId);
    User user=userRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("User with this id doesn't exist here"));
    userRepo.delete(user);
    }

    @Override
    public UserDto getUserById(String userId) {
        User user=userRepo.findById(UserHelper.parseUUID(userId)).orElseThrow(() -> new ResourceNotFoundException("User with this id doesn't exist here"));

        return modelMapper.map(user,UserDto.class);
    }

    @Override
    public Iterable<UserDto> getAllUser() {
        //Iterable<UserDto> users=new Iterable<UserDto>();

        return userRepo.findAll().stream().map(user ->modelMapper.map(user,UserDto.class)).toList();
    }
}
