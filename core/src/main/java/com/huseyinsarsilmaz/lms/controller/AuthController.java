package com.huseyinsarsilmaz.lms.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huseyinsarsilmaz.lms.model.dto.request.LoginRequest;
import com.huseyinsarsilmaz.lms.model.dto.request.RegisterRequest;
import com.huseyinsarsilmaz.lms.model.dto.response.LmsApiResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.LoginResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.RegisterResponse;
import com.huseyinsarsilmaz.lms.model.entity.User;
import com.huseyinsarsilmaz.lms.service.AuthService;
import com.huseyinsarsilmaz.lms.service.UserService;
import com.huseyinsarsilmaz.lms.util.LmsResponseBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final LmsResponseBuilder responseBuilder;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user in the system. Admin registration requires a valid secret code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegisterResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Invalid admin secret code or admin already exists", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict - When a user with given email already exists", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation failed", content = @Content)
    })

    public ResponseEntity<LmsApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {

        User registeredUser = userService.register(request);
        RegisterResponse response = new RegisterResponse(registeredUser);

        return responseBuilder.success("User", "registered", response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user and generates a JWT token for further requests.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful - JWT token returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation failed", content = @Content)
    })
    public ResponseEntity<LmsApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        String jwtToken = authService.authenticateAndGenerateToken(request);
        LoginResponse response = new LoginResponse(jwtToken);

        return responseBuilder.success("User", "logged in", response, HttpStatus.OK);
    }
}
