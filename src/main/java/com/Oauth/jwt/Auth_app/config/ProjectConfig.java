
package com.Oauth.jwt.Auth_app.config;

import com.Oauth.jwt.Auth_app.dtos.UserDto;
import com.Oauth.jwt.Auth_app.entites.User;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjectConfig {
//creates a ModelMapper and configures it to skip the roles property when converting UserDto into User
    @Bean
    public ModelMapper modelMapper() {

        ModelMapper modelMapper = new ModelMapper();

        modelMapper.typeMap(UserDto.class, User.class)
                .addMappings(mapper -> mapper.skip(User::setRoles));

        return modelMapper;
    }
}
