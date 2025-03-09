package com.spring_portfolio.mvc.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique=true)
    private String name;

    public UserRole(String name) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }
    public static UserRole[] init() {
        UserRole customer = new UserRole("ROLE_USER");
        UserRole admin = new UserRole("ROLE_ADMIN");
        UserRole[] initArray = {customer, admin};
        return initArray;
    }
}
