package com.Oauth.jwt.Auth_app;

import com.Oauth.jwt.Auth_app.config.AppContants;
import com.Oauth.jwt.Auth_app.entites.Role;
import com.Oauth.jwt.Auth_app.repo.RoleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.UUID;

@SpringBootApplication
public class AuthAppApplication implements CommandLineRunner {

	@Autowired
	private RoleRepo roleRepo;//not static varibale cannot be used under static method instance cannot be used under static context so for this we are using commanline runner
	public static void main(String[] args) {
		SpringApplication.run(AuthAppApplication.class, args);
	}


	@Override//non static hai and rolerepo b non static hai to use hoskta hai
	public void run(String... args) throws Exception {
		//we will create default user roles here
		roleRepo.findByName("ROLE_"+AppContants.ADMIN_ROLE).ifPresentOrElse(role->{
				System.out.println("Admin role already there"+role.getName());
				},()->{
			//if role is not present assign admin role in the db
					Role role=new Role();
					role.setName("ROLE_"+AppContants.ADMIN_ROLE);
					role.setId(UUID.randomUUID());
					roleRepo.save(role);
				}


				);

		roleRepo.findByName("ROLE_"+AppContants.GUEST_ROLE).ifPresentOrElse(role->{
			System.out.println("Guest role is  already there"+role.getName());
				},()->{
					//if role is not present assign admin role in the db
					Role role=new Role();
					role.setName("ROLE_"+AppContants.GUEST_ROLE);
					role.setId(UUID.randomUUID());
					roleRepo.save(role);
				}


		);
	}
}
