package com.spring_portfolio.mvc;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import com.spring_portfolio.mvc.user.User;
import com.spring_portfolio.mvc.user.DetailsService;
import com.spring_portfolio.mvc.user.UserRole;
import com.spring_portfolio.mvc.user.UserRoleJpaRepository;


import java.util.List;
import java.util.ArrayList;

@Component
@Configuration // Scans Application for ModelInit Bean, this detects CommandLineRunner
public class ModelInit {  
    @Autowired DetailsService userService;
    @Autowired UserRoleJpaRepository roleRepo;

    @Bean
    CommandLineRunner run() {  // The run() method will be executed after the application starts
        return args -> {

            UserRole[] userRoles = UserRole.init();
            for (UserRole role : userRoles) {
                UserRole existingRole = roleRepo.findByName(role.getName());
                if (existingRole != null) {
                    // role already exists
                    continue;
                } else {
                    // role doesn't exist
                    roleRepo.save(role);
                }
            }

            // user database is populated with test data
            User[] userArray = User.init();
            
            for (User user : userArray) {
                //findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase
                List<User> userFound = userService.list(user.getName(), user.getEmail());  // lookup
                if (userFound.size() == 0) {
                    userService.save(user);  // save
                    userService.addRoleToUser(user.getEmail(), "ROLE_USER");
                    
                }
            }
            for (int i = 1; i <= 3 && i < userArray.length; i++) {
                userService.addRoleToUser(userArray[i].getEmail(), "ROLE_ADMIN");
            }

        

        };
    }
}