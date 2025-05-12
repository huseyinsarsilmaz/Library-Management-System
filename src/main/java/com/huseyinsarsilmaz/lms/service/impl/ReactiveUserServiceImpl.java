package com.huseyinsarsilmaz.lms.service.impl;

import org.springframework.stereotype.Service;

import com.huseyinsarsilmaz.lms.model.entity.User;
import com.huseyinsarsilmaz.lms.repository.ReactiveUserRepository;
import com.huseyinsarsilmaz.lms.service.ReactiveUserService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReactiveUserServiceImpl implements ReactiveUserService {

    private final ReactiveUserRepository userRepository;

    @Override
    public Mono<User> changeActive(User user, boolean active) {
        user.setIsActive(active);
        return userRepository.save(user);
    }
}
