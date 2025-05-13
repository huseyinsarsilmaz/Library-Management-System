package com.huseyinsarsilmaz.lmsr.service;

import org.springframework.stereotype.Service;

import com.huseyinsarsilmaz.lmsr.model.entity.User;
import com.huseyinsarsilmaz.lmsr.repository.UserRepository;

import reactor.core.publisher.Mono;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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
