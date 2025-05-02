package com.huseyinsarsilmaz.lms.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.huseyinsarsilmaz.lms.model.dto.request.RegisterRequest;
import com.huseyinsarsilmaz.lms.model.entity.User;
import com.huseyinsarsilmaz.lms.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User register(RegisterRequest req) {
        // TODO Tightly coupled will be handled
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        req.setPassword(encoder.encode(req.getPassword()));

        User newUser = User.builder()
                .email(req.getEmail())
                .password(req.getPassword())
                .name(req.getName())
                .surname(req.getSurname())
                .phoneNumber(req.getPhoneNumber())
                .build();

        return userRepository.save(newUser);
    }

}
