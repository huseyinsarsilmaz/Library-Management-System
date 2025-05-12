package com.huseyinsarsilmaz.lms.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.huseyinsarsilmaz.lms.model.dto.request.LoginRequest;
import com.huseyinsarsilmaz.lms.security.JwtService;
import com.huseyinsarsilmaz.lms.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public String authenticateAndGenerateToken(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        return jwtService.generateToken(request.getEmail());
    }
}
