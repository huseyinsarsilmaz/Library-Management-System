package com.huseyinsarsilmaz.lmsr.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.huseyinsarsilmaz.lmsr.model.entity.User;

import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    Mono<User> findByEmail(String email);

}