package com.huseyinsarsilmaz.lms.service;

import java.util.Optional;

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
    private final PasswordEncoder passwordEncoder;

    public void isEmailTaken(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("An User with this email already exists");
        }
    }

    public User register(RegisterRequest req) {
        req.setPassword(passwordEncoder.encode(req.getPassword()));

        User newUser = User.builder()
                .email(req.getEmail())
                .password(req.getPassword())
                .name(req.getName())
                .surname(req.getSurname())
                .phoneNumber(req.getPhoneNumber())
                .build();

        return userRepository.save(newUser);
    }

    public User getByEmail(String email) {
        Optional<User> optUser = userRepository.findByEmail(email);
        if (optUser.isEmpty()) {
            throw new RuntimeException("There is no user with this Email");
        }

        return optUser.get();
    }

}
