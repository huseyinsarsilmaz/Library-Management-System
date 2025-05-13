package com.huseyinsarsilmaz.lms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.huseyinsarsilmaz.lms.model.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByRoles(String roles);

    boolean existsByIdAndIsActiveFalse(Long id);
}