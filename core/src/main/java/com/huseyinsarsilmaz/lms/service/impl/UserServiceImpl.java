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
import com.huseyinsarsilmaz.lms.exception.UserPromotionException;
import com.huseyinsarsilmaz.lms.exception.NotFoundException;
import com.huseyinsarsilmaz.lms.exception.PassordNotMatchedException;
import com.huseyinsarsilmaz.lms.model.dto.request.PasswordUpdateRequest;
import com.huseyinsarsilmaz.lms.model.dto.request.RegisterRequest;
import com.huseyinsarsilmaz.lms.model.dto.request.UserUpdateRequest;
import com.huseyinsarsilmaz.lms.model.entity.User;
import com.huseyinsarsilmaz.lms.model.mapper.UserMapper;
import com.huseyinsarsilmaz.lms.repository.UserRepository;
import com.huseyinsarsilmaz.lms.service.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private static final String SECRET_ADMIN_CODE = "aHN5bnNhcnNpbG1heg==";

    private static final String ADMIN_ROLE = "ROLE_ADMIN,ROLE_LIBRARIAN,ROLE_PATRON";

    private Set<String> parseRoles(String rolesCsv) {
        return new HashSet<>(Arrays.asList(rolesCsv.split(",")));
    }

    public void isEmailTaken(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new AlreadyExistsException(User.class.getSimpleName(), "email");
        }
    }

    public User register(RegisterRequest req) {
        isEmailTaken(req.getEmail());
        User user = userMapper.toEntity(req);
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        if (req.getSecretCode() != null) {
            if (!SECRET_ADMIN_CODE.equals(req.getSecretCode()) || userRepository.findByRoles(ADMIN_ROLE).isPresent()) {
                throw new ForbiddenException();
            }
            user.setRoles(ADMIN_ROLE);
        } else {
            user.setRoles(User.Role.ROLE_PATRON.name());
        }

        log.info("A new user registered into system: " + user.getEmail());
        return userRepository.save(user);
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User", "email"));
    }


    public User getById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", "id"));
    }

    @Transactional
    public User promote(User user, User.Role newRole) {
        Set<String> roles = parseRoles(user.getRoles());
        if (newRole == User.Role.ROLE_ADMIN || newRole == User.Role.ROLE_PATRON || roles.contains(newRole.name())) {
            throw new UserPromotionException();
        }

        roles.add(newRole.name());

        user.setRoles(String.join(",", roles));
        log.info("User {} is promoted", user.getEmail());
        return userRepository.save(user);
    }

    public void checkHasRole(User user, User.Role requiredRole) {
        Set<String> roles = parseRoles(user.getRoles());

        if (!roles.contains(requiredRole.name())) {
            throw new ForbiddenException();
        }
    }

    @Transactional
    public User update(User user, UserUpdateRequest req) {
        userMapper.updateEntity(user, req);

        return userRepository.save(user);
    }

    @Transactional
    public void delete(User user) {
        userRepository.delete(user);
    }

    public void checkActiveById(Long userId) {
        if (userRepository.existsByIdAndIsActiveFalse(userId)) {
            throw new UserInactiveException();
        }
    }

    @Transactional
    public User changeActive(User user, boolean newActive) {
        user.setIsActive(newActive);
        return userRepository.save(user);
    }

    public void checkDeactivated(User user) {
        if (!userRepository.existsByIdAndIsActiveFalse(user.getId())) {
            throw new UserNotDeactivatedException();
        }
    }

    @Transactional
    public User updatePassword(User user, PasswordUpdateRequest req) {
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new PassordNotMatchedException();
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        return userRepository.save(user);
    }
}
