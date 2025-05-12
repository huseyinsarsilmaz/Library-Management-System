package com.huseyinsarsilmaz.lms.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.huseyinsarsilmaz.lms.model.entity.User;

@Repository
public interface ReactiveUserRepository extends ReactiveCrudRepository<User, Long> {

}
