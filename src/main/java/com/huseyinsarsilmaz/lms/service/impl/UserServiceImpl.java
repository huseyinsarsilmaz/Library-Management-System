package com.huseyinsarsilmaz.lms.service.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.huseyinsarsilmaz.lms.exception.AlreadyExistsException;
import com.huseyinsarsilmaz.lms.exception.ForbiddenException;
import com.huseyinsarsilmaz.lms.exception.UserInactiveException;
import com.huseyinsarsilmaz.lms.exception.UserNotDeactivatedException;
import com.huseyinsarsilmaz.lms.exception.NotFoundException;
import com.huseyinsarsilmaz.lms.model.dto.request.RegisterRequest;
import com.huseyinsarsilmaz.lms.model.dto.request.UserUpdateRequest;
import com.huseyinsarsilmaz.lms.model.entity.User;
import com.huseyinsarsilmaz.lms.model.mapper.UserMapper;
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
    private final UserMapper userMapper;

    public void isEmailTaken(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new AlreadyExistsException(User.class.getSimpleName(), "email");
        }
    }

    public User register(RegisterRequest req) {
        isEmailTaken(req.getEmail());
        req.setPassword(passwordEncoder.encode(req.getPassword()));
        Set<String> roles = new HashSet<>();
        roles.add(User.Role.ROLE_PATRON.name());

        return userRepository.save(userMapper.toEntity(req));
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User", "email"));
    }


    public User getById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", "id"));
    }

    public User promote(User user, User.Role newRole) {
        if (newRole == User.Role.ROLE_ADMIN) {
            throw new ForbiddenException();
        } else if (newRole == User.Role.ROLE_PATRON) {
            return user;
        }

        String[] currentRoles = user.getRoles().split(",");
        Set<String> newRoles = new HashSet<>(Arrays.asList(currentRoles));

        if (newRoles.contains(newRole.name())) {
            return user;
        }

        newRoles.add(newRole.name());

        user.setRoles(String.join(",", newRoles));
        return userRepository.save(user);
    }

    public User getFromToken(String token) {
        token = token.substring(7);
        String email = jwtService.extractEmail(token);
        return getByEmail(email);
    }

    public void checkRole(User user, User.Role requiredRole) {
        String[] rolesArr = user.getRoles().split(",");
        Set<String> roles = new HashSet<>(Arrays.asList(rolesArr));

        if (!roles.contains(requiredRole.name())) {
            throw new ForbiddenException();
        }
    }

    public User update(User user, UserUpdateRequest req) {
        userMapper.updateEntity(user, req);

        return userRepository.save(user);
    }

    public void delete(User user) {
        userRepository.delete(user);
    }

    public void checkActiveById(Long userId) {
        if (userRepository.existsByIdAndIsActiveFalse(userId)) {
            throw new UserInactiveException();
        }
    }

    public User changeActive(User user, boolean newActive) {
        user.setIsActive(newActive);
        return userRepository.save(user);
    }

    public void checkDeactivated(User user) {
        if (!userRepository.existsByIdAndIsActiveFalse(user.getId())) {
            throw new UserNotDeactivatedException();
        }
    }
}
