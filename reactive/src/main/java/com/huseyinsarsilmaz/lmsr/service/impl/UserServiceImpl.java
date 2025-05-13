package com.huseyinsarsilmaz.lmsr.service.impl;

import org.springframework.stereotype.Service;

import com.huseyinsarsilmaz.lmsr.model.entity.User;
import com.huseyinsarsilmaz.lmsr.repository.UserRepository;
import com.huseyinsarsilmaz.lmsr.service.UserService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public Mono<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Mono<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Mono<User> changeActive(Mono<User> userMono, boolean active) {
        return userMono.flatMap(user -> {
            user.setIsActive(active);
            return userRepository.save(user);
        });
    }
}
