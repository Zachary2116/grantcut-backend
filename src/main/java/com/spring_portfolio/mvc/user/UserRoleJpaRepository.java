package com.spring_portfolio.mvc.user;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface  UserRoleJpaRepository extends JpaRepository<UserRole, Long> {
    UserRole findByName(String name);
    List<UserRole> findAll();
}