package com.huseyinsarsilmaz.lms.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huseyinsarsilmaz.lms.model.dto.response.ApiResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.UserSimple;
import com.huseyinsarsilmaz.lms.model.entity.User;
import com.huseyinsarsilmaz.lms.repository.UserRepository;
import com.huseyinsarsilmaz.lms.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private final RestTemplate restTemplate = new RestTemplate();

    private String baseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private User librarianUser;
    private User patronUser;
    private String librarianToken;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/users";

        userRepository.deleteAll();

        librarianUser = userRepository.save(User.builder()
                .email("huseyinsarsilmaz@hotmail.com")
                .password("MTIzNDU2Nzg")
                .roles("ROLE_LIBRARIAN,ROLE_PATRON")
                .isActive(true)
                .build());

        patronUser = userRepository.save(User.builder()
                .email("huseyinsarsilmaz2@hotmail.com")
                .password("MTIzNDU2Nzg")
                .roles("ROLE_PATRON")
                .isActive(true)
                .build());

        librarianToken = jwtService.generateToken(librarianUser.getEmail());
    }

    @Test
    void getUser_shouldReturnpatronUser_whenAuthorized() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(librarianToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + patronUser.getId(),
                HttpMethod.GET,
                requestEntity,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ApiResponse<UserSimple> apiResponse = objectMapper.readValue(
                response.getBody(),
                new TypeReference<ApiResponse<UserSimple>>() {
                });
        UserSimple userSimple = objectMapper.convertValue(apiResponse.getData(), UserSimple.class);

        assertThat(userSimple.getEmail()).isEqualTo(patronUser.getEmail());
        assertThat(userSimple.getId()).isEqualTo(patronUser.getId());
    }
}
