package com.huseyinsarsilmaz.lmsr.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huseyinsarsilmaz.lmsr.model.dto.request.LoginRequest;
import com.huseyinsarsilmaz.lmsr.model.dto.response.LoginResponse;
import com.huseyinsarsilmaz.lmsr.security.JwtService;
import com.huseyinsarsilmaz.lmsr.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthCotroller {

    private final JwtService jwtService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(@Valid @RequestBody Mono<LoginRequest> req) {
        return req
                .flatMap(loginRequest -> userService.getUserByEmail(loginRequest.getEmail())
                        .switchIfEmpty(Mono.error(new RuntimeException("There is no user with this email")))
                        .flatMap(user -> {
                            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                                return Mono.error(new RuntimeException("The password is wrong"));
                            }

                            List<String> roles = Arrays.asList(user.getRoles().split(","));
                            String token = jwtService.generateToken(user.getEmail(), roles);
                            return Mono.just(ResponseEntity.ok(new LoginResponse(token)));
                        }));
    }
}