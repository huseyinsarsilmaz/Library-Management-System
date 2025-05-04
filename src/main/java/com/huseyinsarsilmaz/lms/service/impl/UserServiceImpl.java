package com.huseyinsarsilmaz.lms.service.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.huseyinsarsilmaz.lms.exception.AlreadyExistsException;
import com.huseyinsarsilmaz.lms.exception.ForbiddenException;
import com.huseyinsarsilmaz.lms.exception.NotFoundException;
import com.huseyinsarsilmaz.lms.model.dto.request.RegisterRequest;
import com.huseyinsarsilmaz.lms.model.entity.User;
import com.huseyinsarsilmaz.lms.repository.UserRepository;
import com.huseyinsarsilmaz.lms.security.JwtService;
import com.huseyinsarsilmaz.lms.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public void isEmailTaken(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new AlreadyExistsException(User.class.getSimpleName(), "email");
        }
    }

    public User register(RegisterRequest req) {
        req.setPassword(passwordEncoder.encode(req.getPassword()));
        Set<String> roles = new HashSet<>();
        roles.add(User.Role.ROLE_LIBRARIAN.name());

        User newUser = User.builder()
                .email(req.getEmail())
                .password(req.getPassword())
                .name(req.getName())
                .surname(req.getSurname())
                .phoneNumber(req.getPhoneNumber())
                .roles(String.join(",", roles))
                .build();

        return userRepository.save(newUser);
    }

    public User getByEmail(String email) {
        Optional<User> optUser = userRepository.findByEmail(email);
        if (optUser.isEmpty()) {
            throw new NotFoundException(User.class.getSimpleName(), "email");
        }

        return optUser.get();
    }

    public User promote(User user, User.Role newRole) {
        if (newRole == User.Role.ROLE_ADMIN) {
            throw new ForbiddenException();
        } else if (newRole == User.Role.ROLE_PATRON) {
            return user;
        }

        String[] currentRoles = user.getRoles().split(",");
        Set<String> newRoles = new HashSet<>(Arrays.asList(currentRoles));

        newRoles.add(newRole.name());

        user.setRoles(String.join(",", newRoles));
        return userRepository.save(user);
    }

    public User getUserFromToken(String token) {
        token = token.substring(7);
        String email = jwtService.extractEmail(token);
        return getByEmail(email);
    }

    public void checkUserRole(User user, User.Role requiredRole) {
        String[] rolesArr = user.getRoles().split(",");
        Set<String> roles = new HashSet<>(Arrays.asList(rolesArr));

        if (!roles.contains(requiredRole.name())) {
            throw new ForbiddenException();
        }
    }

}
