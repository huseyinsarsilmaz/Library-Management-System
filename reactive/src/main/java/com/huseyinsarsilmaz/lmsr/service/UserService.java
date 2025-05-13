package com.huseyinsarsilmaz.lmsr.service;

import com.huseyinsarsilmaz.lmsr.model.entity.User;

import reactor.core.publisher.Mono;

public interface UserService {

    public Mono<User> getUserByEmail(String email);

    public Mono<User> getUserById(Long id);

    public Mono<User> changeActive(Mono<User> userMono, boolean active);
}
