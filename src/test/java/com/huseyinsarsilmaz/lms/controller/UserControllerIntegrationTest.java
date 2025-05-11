package com.huseyinsarsilmaz.lms.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huseyinsarsilmaz.lms.model.dto.request.PromoteRequest;
import com.huseyinsarsilmaz.lms.model.dto.request.UserUpdateRequest;
import com.huseyinsarsilmaz.lms.model.dto.response.ApiResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.PromoteResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.UserDetailed;
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
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

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
        private User adminUser;

        private String toJson(Object obj) {
                try {
                        return objectMapper.writeValueAsString(obj);
                } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to serialize object", e);
                }
        }

        private HttpEntity<String> createEntity(Object request, String email) {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + jwtService.generateToken(email));
                headers.setContentType(MediaType.APPLICATION_JSON);
                String json = request != null ? toJson(request) : null;
                return new HttpEntity<>(json, headers);
        }

        @BeforeEach
        void setUp() {
                baseUrl = "http://localhost:" + port + "/api/users";

                restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
                        @Override
                        public boolean hasError(@NonNull ClientHttpResponse response) throws IOException {
                                // Never throw exceptions for HTTP errors
                                return false;
                        }
                });

                userRepository.deleteAll();

                librarianUser = userRepository.save(User.builder()
                                .email("huseyinsarsilmaz@hotmail.com")
                                .password("MTIzNDU2Nzg")
                                .roles("ROLE_LIBRARIAN,ROLE_PATRON")
                                .phoneNumber("1234567891")
                                .name("Hüseyin")
                                .surname("Sarsılmaz")
                                .isActive(true)
                                .build());

                patronUser = userRepository.save(User.builder()
                                .email("huseyinsarsilmaz2@hotmail.com")
                                .password("MTIzNDU2Nzg")
                                .roles("ROLE_PATRON")
                                .phoneNumber("1234567892")
                                .name("Hüseyinn")
                                .surname("Sarsılmazz")
                                .isActive(true)
                                .build());

                adminUser = userRepository.save(User.builder()
                                .email("huseyinsarsilmaz3@hotmail.com")
                                .password("MTIzNDU2Nzg")
                                .roles("ROLE_ADMIN,ROLE_LIBRARIAN,ROLE_PATRON")
                                .phoneNumber("1234567893")
                                .name("Hüseyinnn")
                                .surname("Sarsılmazzz")
                                .isActive(true)
                                .build());

        }

        @Test
        void testPromote_whenMyUserNotExists() {
                String fakeToken = jwtService.generateToken("nonexistent@hotmail.com");

                PromoteRequest request = new PromoteRequest(patronUser.getEmail(),
                                User.Role.ROLE_LIBRARIAN);

                HttpEntity<String> entity = createEntity(request, fakeToken);

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/promote",
                                entity,
                                String.class);

                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        void testPromote_whenUserIsNotAdmin() {
                PromoteRequest request = new PromoteRequest(patronUser.getEmail(),
                                User.Role.ROLE_LIBRARIAN);

                HttpEntity<String> entity = createEntity(request, librarianUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/promote",
                                entity,
                                String.class);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testPromote_whenPromotedUserNotExists() {
                PromoteRequest request = new PromoteRequest("doesnotexist@hotmail.com",
                                User.Role.ROLE_LIBRARIAN);

                HttpEntity<String> entity = createEntity(request, adminUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/promote",
                                entity,
                                String.class);

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        void testPromote_whenNewRoleIsAdmin() {
                PromoteRequest request = new PromoteRequest(patronUser.getEmail(),
                                User.Role.ROLE_ADMIN);

                HttpEntity<String> entity = createEntity(request, adminUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/promote",
                                entity,
                                String.class);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testPromote_whenHappyPath() throws JsonProcessingException {
                PromoteRequest request = new PromoteRequest(patronUser.getEmail(),
                                User.Role.ROLE_LIBRARIAN);

                HttpEntity<String> entity = createEntity(request, adminUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/promote",
                                entity,
                                String.class);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                ApiResponse<PromoteResponse> apiResponse = objectMapper.readValue(
                                response.getBody(),
                                new TypeReference<ApiResponse<PromoteResponse>>() {
                                });

                assertEquals(patronUser.getEmail(), apiResponse.getData().getEmail());
        }

        @Test
        void testGetMyUser_whenMyUserDoesNotExist() {
                HttpEntity<String> entity = createEntity(null, "ghostuser@example.com");

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/me",
                                HttpMethod.GET,
                                entity,
                                String.class);

                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        void testGetMyUser_whenMyUserExists() throws JsonProcessingException {
                HttpEntity<String> entity = createEntity(null, patronUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/me",
                                HttpMethod.GET,
                                entity,
                                String.class);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                ApiResponse<UserSimple> apiResponse = objectMapper.readValue(
                                response.getBody(),
                                new TypeReference<ApiResponse<UserSimple>>() {
                                });

                assertEquals(patronUser.getEmail(), apiResponse.getData().getEmail());
        }

        @Test
        void testUpdateMyUser_whenMyUserDoesNotExist() throws JsonProcessingException {
                UserUpdateRequest updateRequest = new UserUpdateRequest();
                updateRequest.setEmail(patronUser.getEmail());
                updateRequest.setName("Updated Name");
                updateRequest.setSurname(patronUser.getSurname());
                updateRequest.setPhoneNumber(patronUser.getPhoneNumber());

                HttpEntity<String> entity = createEntity(updateRequest, "ghostuser@example.com");

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/me",
                                HttpMethod.PUT,
                                entity,
                                String.class);

                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        }

        @Test
        void testUpdateMyUser_whenEmailIsUnchanged() throws JsonProcessingException {
                UserUpdateRequest updateRequest = new UserUpdateRequest();
                updateRequest.setEmail(patronUser.getEmail());
                updateRequest.setName("Updated");
                updateRequest.setSurname(patronUser.getSurname());
                updateRequest.setPhoneNumber(patronUser.getPhoneNumber());

                HttpEntity<String> entity = createEntity(updateRequest, patronUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/me",
                                HttpMethod.PUT,
                                entity,
                                String.class);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                ApiResponse<UserSimple> apiResponse = objectMapper.readValue(
                                response.getBody(),
                                new TypeReference<ApiResponse<UserSimple>>() {
                                });

                assertEquals("Updated", apiResponse.getData().getName());
        }

        @Test
        void testUpdateMyUser_whenEmailIsTaken() {
                UserUpdateRequest updateRequest = new UserUpdateRequest();
                updateRequest.setEmail(adminUser.getEmail());
                updateRequest.setName("Updated");
                updateRequest.setSurname(patronUser.getSurname());
                updateRequest.setPhoneNumber(patronUser.getPhoneNumber());

                HttpEntity<String> entity = createEntity(updateRequest, patronUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/me",
                                HttpMethod.PUT,
                                entity,
                                String.class);

                assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        }

        @Test
        void testDeleteMyUser_whenMyUserExists() throws JsonProcessingException {
                HttpEntity<String> entity = createEntity(null, patronUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/me",
                                HttpMethod.DELETE,
                                entity,
                                String.class);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                ApiResponse<UserSimple> apiResponse = objectMapper.readValue(
                                response.getBody(),
                                new TypeReference<ApiResponse<UserSimple>>() {
                                });

                assertEquals(patronUser.getEmail(), apiResponse.getData().getEmail());

                User deleted = userRepository.findById(patronUser.getId()).orElse(null);
                assertNull(deleted);
        }

        @Test
        void testDeleteMyUser_whenMyUserDoesNotExist() {
                String fakeToken = jwtService.generateToken("ghostuser@example.com");
                HttpEntity<String> entity = createEntity(null, fakeToken);

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/me",
                                HttpMethod.DELETE,
                                entity,
                                String.class);

                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        void testGetUser_whenPatronTriesToReadLibrarian() {
                HttpEntity<String> entity = createEntity(null, patronUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/" + librarianUser.getId(),
                                HttpMethod.GET,
                                entity,
                                String.class);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testGetUser_whenLibrarianReadsPatron() {
                HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/" + patronUser.getId(),
                                HttpMethod.GET,
                                entity,
                                String.class);

                assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        void testGetUser_whenLibrarianReadsAdmin() {
                HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/" + adminUser.getId(),
                                HttpMethod.GET,
                                entity,
                                String.class);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testUpdateUser_whenPatronTriesToModifyLibrarian() {
                UserUpdateRequest updateRequest = new UserUpdateRequest();
                updateRequest.setEmail(librarianUser.getEmail());
                updateRequest.setName("Updated");
                updateRequest.setSurname(librarianUser.getSurname());
                updateRequest.setPhoneNumber(librarianUser.getPhoneNumber());

                HttpEntity<String> entity = createEntity(updateRequest, patronUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/" + librarianUser.getId(),
                                HttpMethod.PUT,
                                entity,
                                String.class);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testUpdateUser_whenLibrarianTriesToModifyLibrarian() {
                User librarianUser2 = userRepository.save(User.builder()
                                .email("huseyinsarsilmaz4@hotmail.com")
                                .password("MTIzNDU2Nzg")
                                .roles("ROLE_LIBRARIAN,ROLE_PATRON")
                                .phoneNumber("1234567891")
                                .name("Hüseyin")
                                .surname("Sarsılmaz")
                                .isActive(true)
                                .build());

                UserUpdateRequest updateRequest = new UserUpdateRequest();
                updateRequest.setEmail(librarianUser2.getEmail());
                updateRequest.setName("Updated");
                updateRequest.setSurname(librarianUser2.getSurname());
                updateRequest.setPhoneNumber(librarianUser2.getPhoneNumber());
                HttpEntity<String> entity = createEntity(updateRequest, librarianUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/" + librarianUser2.getId(),
                                HttpMethod.PUT,
                                entity,
                                String.class);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testUpdateUser_whenLibrarianModifiesPatron() throws JsonProcessingException {
                UserUpdateRequest updateRequest = new UserUpdateRequest();
                updateRequest.setEmail(patronUser.getEmail());
                updateRequest.setName("Updated");
                updateRequest.setSurname(patronUser.getSurname());
                updateRequest.setPhoneNumber(patronUser.getPhoneNumber());
                HttpEntity<String> entity = createEntity(updateRequest, librarianUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/" + patronUser.getId(),
                                HttpMethod.PUT,
                                entity,
                                String.class);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                ApiResponse<UserSimple> apiResponse = objectMapper.readValue(
                                response.getBody(), new TypeReference<ApiResponse<UserSimple>>() {
                                });

                assertEquals("Updated", apiResponse.getData().getName());
        }

        @Test
        void testUpdateUser_whenAdminModifiesLibrarian() throws JsonProcessingException {
                UserUpdateRequest updateRequest = new UserUpdateRequest();
                updateRequest.setEmail(librarianUser.getEmail());
                updateRequest.setName("Updated");
                updateRequest.setSurname(librarianUser.getSurname());
                updateRequest.setPhoneNumber(librarianUser.getPhoneNumber());
                HttpEntity<String> entity = createEntity(updateRequest, adminUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/" + librarianUser.getId(),
                                HttpMethod.PUT,
                                entity,
                                String.class);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                ApiResponse<UserSimple> apiResponse = objectMapper.readValue(
                                response.getBody(), new TypeReference<ApiResponse<UserSimple>>() {
                                });

                assertEquals("Updated", apiResponse.getData().getName());
        }

        @Test
        void testDeleteUser_whenPatronTriesToDeleteLibrarian() {
                HttpEntity<String> entity = createEntity(null, patronUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/" + librarianUser.getId(),
                                HttpMethod.DELETE,
                                entity,
                                String.class);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testDeleteUser_whenLibrarianTriesToDeleteLibrarian() {
                User librarianUser2 = userRepository.save(User.builder()
                                .email("librarian2@example.com")
                                .password("MTIzNDU2Nzg")
                                .roles("ROLE_LIBRARIAN,ROLE_PATRON")
                                .phoneNumber("9876543210")
                                .name("Second")
                                .surname("Librarian")
                                .isActive(true)
                                .build());

                HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/" + librarianUser2.getId(),
                                HttpMethod.DELETE,
                                entity,
                                String.class);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testDeleteUser_whenLibrarianDeletesPatron() throws JsonProcessingException {
                HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/" + patronUser.getId(),
                                HttpMethod.DELETE,
                                entity,
                                String.class);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                ApiResponse<UserSimple> apiResponse = objectMapper.readValue(
                                response.getBody(), new TypeReference<ApiResponse<UserSimple>>() {
                                });

                User deleted = userRepository.findById(apiResponse.getData().getId()).orElse(null);
                assertNull(deleted);

        }

        @Test
        void testDeleteUser_whenAdminDeletesLibrarian() throws JsonProcessingException {
                HttpEntity<String> entity = createEntity(null, adminUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/" + librarianUser.getId(),
                                HttpMethod.DELETE,
                                entity,
                                String.class);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                ApiResponse<UserSimple> apiResponse = objectMapper.readValue(
                                response.getBody(), new TypeReference<ApiResponse<UserSimple>>() {
                                });

                User deleted = userRepository.findById(apiResponse.getData().getId()).orElse(null);
                assertNull(deleted);

        }

        @Test
        void testReactivateUser_whenMyUserNotExists() {
                HttpEntity<String> entity = createEntity(null, "nonexistent@email.com");

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/" + patronUser.getId() + "/reactivate",
                                HttpMethod.POST,
                                entity,
                                String.class);

                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        void testReactivateUser_whenRoleIsNotEnough() {
                HttpEntity<String> entity = createEntity(null, patronUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/" + librarianUser.getId() + "/reactivate",
                                HttpMethod.POST,
                                entity,
                                String.class);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testReactivateUser_whenReactivatedUserNotExists() {
                HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/99999/reactivate",
                                HttpMethod.POST,
                                entity,
                                String.class);

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        void testReactivateUser_whenUserIsAlreadyActive() {
                HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/" + patronUser.getId() + "/reactivate",
                                HttpMethod.POST,
                                entity,
                                String.class);

                assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        }

        @Test
        void testReactivateUser_whenHappyPath() throws JsonProcessingException {
                User deactivatedUser = userRepository.save(User.builder()
                                .email("deactivated@example.com")
                                .password("MTIzNDU2Nzg")
                                .roles("ROLE_PATRON")
                                .name("Deactivated")
                                .surname("User")
                                .phoneNumber("1111111111")
                                .isActive(false)
                                .build());

                HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/" + deactivatedUser.getId() + "/reactivate",
                                HttpMethod.POST,
                                entity,
                                String.class);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                ApiResponse<UserDetailed> apiResponse = objectMapper.readValue(
                                response.getBody(), new TypeReference<ApiResponse<UserDetailed>>() {
                                });

                assertEquals(deactivatedUser.getEmail(), apiResponse.getData().getEmail());
                assertTrue(apiResponse.getData().isActive());
        }

}
