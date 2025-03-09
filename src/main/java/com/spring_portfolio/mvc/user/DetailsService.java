package com.spring_portfolio.mvc.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/*
This class has an instance of Java Persistence API (JPA)
-- @Autowired annotation. Allows Spring to resolve and inject collaborating beans into our bean.
-- Spring Data JPA will generate a proxy instance
-- Below are some CRUD methods that we can use with our database
*/
@Service
@Transactional
public class DetailsService implements UserDetailsService {  // "implements" ties ModelRepo to Spring Security
    // Encapsulate many object into a single Bean (user, Roles, and Scrum)
    @Autowired  // Inject userJpaRepository
    private UserJpaRepository repository;
    @Autowired  // Inject RoleJpaRepository
    private UserRoleJpaRepository userRoleJpaRepository;
    // @Autowired  // Inject PasswordEncoder
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    /* UserDetailsService Overrides and maps user & Roles POJO into Spring Security */
    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = repository.findByEmail(email); // setting variable user equal to the method finding the username in the database
        if(user==null) {
			throw new UsernameNotFoundException("User not found with username: " + email);
        }
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        user.getRoles().forEach(role -> { //loop through roles
            authorities.add(new SimpleGrantedAuthority(role.getName())); //create a SimpleGrantedAuthority by passed in role, adding it all to the authorities list, list of roles gets past in for spring security
        });
        // train spring security to User and Authorities
        authorities.add(new SimpleGrantedAuthority("USER_ID_" + user.getId()));
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
    }

    /* user Section */

    public  List<User>listAll() {
        return repository.findAllByOrderByNameAsc();
    }

    // custom query to find match to name or email
    public  List<User>list(String name, String email) {
        return repository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(name, email);
    }

    // custom query to find anything containing term in name or email ignoring case
    public  List<User>listLike(String term) {
        return repository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(term, term);
    }

    // custom query to find anything containing term in name or email ignoring case
    public  List<User>listLikeNative(String term) {
        String like_term = String.format("%%%s%%",term);  // Like required % rappers
        return repository.findByLikeTermNative(like_term);
    }

    // encode password prior to sava
    public void save(User user) {
        user.setPassword(passwordEncoder().encode(user.getPassword()));
        repository.save(user);
    }

    public User get(long id) {
        return (repository.findById(id).isPresent())
                ? repository.findById(id).get()
                : null;
    }

    public User getByEmail(String email) {
        return (repository.findByEmail(email));
    }

    public void delete(long id) {
        repository.deleteById(id);
    }

    public void defaults(String password) {
        for (User user: listAll()) {
            if (user.getPassword() == null || user.getPassword().isEmpty() || user.getPassword().isBlank()) {
                user.setPassword(passwordEncoder().encode(password));
            }
        }
    }
    public List<UserRole> listAllRoles() {
        return userRoleJpaRepository.findAll();
    }
    public UserRole findRole(String roleName) {
        return userRoleJpaRepository.findByName(roleName);
    }

    public void addRoleToUser(String email, String roleName) { // by passing in the two strings you are giving the user that certain role
        System.out.println("adding" + roleName + " to " + email);
        User user = repository.findByEmail(email);
        if (user != null) {   // verify user
            UserRole role = userRoleJpaRepository.findByName(roleName);
            if (role != null) { // verify role
                boolean addRole = true;
                for (UserRole roleObj : user.getRoles()) {    // only add if user is missing role
                    if (roleObj.getName().equals(roleName)) {
                        addRole = false;
                        System.out.println(email + " Already has " + roleName);
                        break;
                    }
                }
                if (addRole) user.getRoles().add(role); 
                System.out.println(email + " has role " + roleName);  // everything is valid for adding role
            }
            else {
                System.out.println("verifying role failed");
            }
        }
        else {
            System.out.println("verifying user failed");
        }
    }
    
}
