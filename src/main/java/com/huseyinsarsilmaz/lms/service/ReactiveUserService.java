package com.huseyinsarsilmaz.lms.service;

import com.huseyinsarsilmaz.lms.model.entity.User;

import reactor.core.publisher.Mono;

public interface ReactiveUserService {
    Mono<User> changeActive(User user, boolean active);
}
