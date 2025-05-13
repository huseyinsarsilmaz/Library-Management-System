package com.huseyinsarsilmaz.lms.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.huseyinsarsilmaz.lms.model.dto.request.LoginRequest;
import com.huseyinsarsilmaz.lms.model.dto.request.RegisterRequest;
import com.huseyinsarsilmaz.lms.model.dto.response.LmsApiResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.LoginResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.RegisterResponse;
import com.huseyinsarsilmaz.lms.model.entity.User;
import com.huseyinsarsilmaz.lms.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String baseUrl;

    private static final String REGISTER_EMAIL = "newuser@example.com";
    private static final String EXISTING_EMAIL = "existing@example.com";
    private static final String PASSWORD = "SecurePassword123";

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private HttpEntity<String> createEntity(Object request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String json = request != null ? toJson(request) : null;
        return new HttpEntity<>(json, headers);
    }

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/auth";

        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(@NonNull ClientHttpResponse response) throws IOException {
                return false;
            }
        });

        userRepository.deleteAll();

        userRepository.save(User.builder()
                .email(EXISTING_EMAIL)
                .password(passwordEncoder.encode(PASSWORD))
                .roles("ROLE_PATRON")
                .phoneNumber("1111111111")
                .name("Exist")
                .surname("User")
                .isActive(true)
                .build());
    }

    @Test
    void testRegister_whenValidRequest_thenCreatesUser() throws Exception {
        RegisterRequest request = new RegisterRequest(
                REGISTER_EMAIL,
                PASSWORD,
                "Test",
                "User",
                "1234567890",
                null);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/register", createEntity(request),
                String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        LmsApiResponse<RegisterResponse> apiResponse = objectMapper.readValue(response.getBody(),
                new TypeReference<>() {
        });
        assertEquals(REGISTER_EMAIL, apiResponse.getData().getEmail());
    }

    @Test
    void testRegister_whenEmailExists_thenConflict() {
        RegisterRequest request = new RegisterRequest(
                EXISTING_EMAIL,
                PASSWORD,
                "Test",
                "User",
                "1234567890", null);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/register", createEntity(request),
                String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void testLogin_whenValidCredentials_thenReturnsToken() throws Exception {
        LoginRequest request = new LoginRequest(EXISTING_EMAIL, PASSWORD);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/login", createEntity(request),
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        LmsApiResponse<LoginResponse> apiResponse = objectMapper.readValue(response.getBody(), new TypeReference<>() {
        });
        assertNotNull(apiResponse.getData().getToken());
    }

    @Test
    void testLogin_whenInvalidCredentials_thenUnauthorized() {
        LoginRequest request = new LoginRequest(EXISTING_EMAIL, "WrongPassword");

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/login", createEntity(request),
                String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
