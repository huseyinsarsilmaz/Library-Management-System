package com.huseyinsarsilmaz.lms.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huseyinsarsilmaz.lms.model.dto.request.LoginRequest;
import com.huseyinsarsilmaz.lms.model.dto.request.RegisterRequest;
import com.huseyinsarsilmaz.lms.model.dto.response.ApiResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.LoginResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.RegisterResponse;
import com.huseyinsarsilmaz.lms.model.entity.User;
import com.huseyinsarsilmaz.lms.service.AuthService;
import com.huseyinsarsilmaz.lms.service.UserService;
import com.huseyinsarsilmaz.lms.service.Utils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        userService.isEmailTaken(request.getEmail());

        User registeredUser = userService.register(request);
        RegisterResponse response = new RegisterResponse(registeredUser);

        return Utils.successResponse("User", "registered", response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        String jwtToken = authService.authenticateAndGenerateToken(request);
        LoginResponse response = new LoginResponse(jwtToken);

        return Utils.successResponse("User", "logged in", response, HttpStatus.OK);
    }
}
