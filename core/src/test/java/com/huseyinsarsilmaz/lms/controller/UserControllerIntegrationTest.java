package com.huseyinsarsilmaz.lms.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huseyinsarsilmaz.lms.model.dto.request.PromoteRequest;
import com.huseyinsarsilmaz.lms.model.dto.request.UserUpdateRequest;
import com.huseyinsarsilmaz.lms.model.dto.response.LmsApiResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.PromoteResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.UserDetailed;
import com.huseyinsarsilmaz.lms.model.dto.response.UserSimple;
import com.huseyinsarsilmaz.lms.model.entity.User;
import com.huseyinsarsilmaz.lms.repository.BorrowingRepository;
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
        private BorrowingRepository borrowingRepository;

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
                headers.set("Authorization", "Bearer " + createToken(email));
                headers.setContentType(MediaType.APPLICATION_JSON);
                String json = request != null ? toJson(request) : null;
                return new HttpEntity<>(json, headers);
        }

        private String createToken(String email) {
                return jwtService.generateToken(email);
        }

        private User createUser(String email, String roles, String name, String surname, String phone) {
                return userRepository.save(User.builder()
                                .email(email)
                                .password("MTIzNDU2Nzg")
                                .roles(roles)
                                .phoneNumber(phone)
                                .name(name)
                                .surname(surname)
                                .isActive(true)
                                .build());
        }

        private UserUpdateRequest buildUpdateRequest(User user, String newName) {
                UserUpdateRequest request = new UserUpdateRequest();
                request.setEmail(user.getEmail());
                request.setName(newName);
                request.setSurname(user.getSurname());
                request.setPhoneNumber(user.getPhoneNumber());
                return request;
        }

        private <T> LmsApiResponse<T> parseResponse(String json, Class<T> clazz) throws JsonProcessingException {
                JavaType type = objectMapper.getTypeFactory()
                                .constructParametricType(LmsApiResponse.class, clazz);
                return objectMapper.readValue(json, type);
        }

        private ResponseEntity<String> sendRequest(String path, HttpMethod method, HttpEntity<String> entity) {
                return restTemplate.exchange(baseUrl + path, method, entity, String.class);
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

                borrowingRepository.deleteAll();
                userRepository.deleteAll();

                librarianUser = createUser("huseyinsarsilmaz@hotmail.com", "ROLE_LIBRARIAN,ROLE_PATRON", "Hüseyin",
                                "Sarsılmaz",
                                "1234567891");
                patronUser = createUser("huseyinsarsilmaz2@hotmail.com", "ROLE_PATRON", "Hüseyinn", "Sarsılmazz",
                                "1234567892");
                adminUser = createUser("huseyinsarsilmaz3@hotmail.com", "ROLE_ADMIN,ROLE_LIBRARIAN,ROLE_PATRON",
                                "Hüseyinnn",
                                "Sarsılmazzz", "1234567893");
        }

        @Test
        void testPromote_whenMyUserNotExists() {
                String fakeToken = createToken("nonexistent@hotmail.com");

                PromoteRequest request = new PromoteRequest(patronUser.getEmail(), User.Role.ROLE_LIBRARIAN);

                HttpEntity<String> entity = createEntity(request, fakeToken);

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/promote", entity,
                                String.class);

                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        void testPromote_whenUserIsNotAdmin() {
                PromoteRequest request = new PromoteRequest(patronUser.getEmail(), User.Role.ROLE_LIBRARIAN);

                HttpEntity<String> entity = createEntity(request, librarianUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/promote", entity,
                                String.class);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testPromote_whenPromotedUserNotExists() {
                PromoteRequest request = new PromoteRequest("doesnotexist@hotmail.com", User.Role.ROLE_LIBRARIAN);

                HttpEntity<String> entity = createEntity(request, adminUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/promote", entity,
                                String.class);

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        void testPromote_whenNewRoleIsAdmin() {
                PromoteRequest request = new PromoteRequest(patronUser.getEmail(), User.Role.ROLE_ADMIN);

                HttpEntity<String> entity = createEntity(request, adminUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/promote", entity,
                                String.class);

                assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        }

        @Test
        void testPromote_whenNewRoleIsPatron() {
                PromoteRequest request = new PromoteRequest(patronUser.getEmail(), User.Role.ROLE_PATRON);

                HttpEntity<String> entity = createEntity(request, adminUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/promote", entity,
                                String.class);

                assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        }

        @Test
        void testPromote_whenHappyPath() throws JsonProcessingException {
                PromoteRequest request = new PromoteRequest(patronUser.getEmail(), User.Role.ROLE_LIBRARIAN);

                HttpEntity<String> entity = createEntity(request, adminUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/promote", entity,
                                String.class);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                LmsApiResponse<PromoteResponse> apiResponse = parseResponse(response.getBody(), PromoteResponse.class);

                assertEquals(patronUser.getEmail(), apiResponse.getData().getEmail());
        }

        @Test
        void testGetMyUser_whenMyUserDoesNotExist() {
                HttpEntity<String> entity = createEntity(null, "ghostuser@example.com");
                ResponseEntity<String> response = sendRequest("/me", HttpMethod.GET, entity);

                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        void testGetMyUser_whenMyUserExists() throws JsonProcessingException {
                HttpEntity<String> entity = createEntity(null, patronUser.getEmail());
                ResponseEntity<String> response = sendRequest("/me", HttpMethod.GET, entity);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                LmsApiResponse<UserSimple> apiResponse = parseResponse(response.getBody(), UserSimple.class);

                assertEquals(patronUser.getEmail(), apiResponse.getData().getEmail());
        }

        @Test
        void testUpdateMyUser_whenMyUserDoesNotExist() throws JsonProcessingException {
                UserUpdateRequest updateRequest = buildUpdateRequest(patronUser, "Updated Name");
                HttpEntity<String> entity = createEntity(updateRequest, "ghostuser@example.com");
                ResponseEntity<String> response = sendRequest("/me", HttpMethod.PUT, entity);

                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        void testUpdateMyUser_whenEmailIsUnchanged() throws JsonProcessingException {
                UserUpdateRequest updateRequest = buildUpdateRequest(patronUser, "Updated");
                HttpEntity<String> entity = createEntity(updateRequest, patronUser.getEmail());
                ResponseEntity<String> response = sendRequest("/me", HttpMethod.PUT, entity);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                LmsApiResponse<UserSimple> apiResponse = parseResponse(response.getBody(), UserSimple.class);

                assertEquals("Updated", apiResponse.getData().getName());
        }

        @Test
        void testUpdateMyUser_whenEmailIsTaken() {
                UserUpdateRequest updateRequest = buildUpdateRequest(patronUser, "Updated");
                updateRequest.setEmail(adminUser.getEmail());
                HttpEntity<String> entity = createEntity(updateRequest, patronUser.getEmail());
                ResponseEntity<String> response = sendRequest("/me", HttpMethod.PUT, entity);

                assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        }

        @Test
        void testDeleteMyUser_whenMyUserExists() throws JsonProcessingException {
                HttpEntity<String> entity = createEntity(null, patronUser.getEmail());
                ResponseEntity<String> response = sendRequest("/me", HttpMethod.DELETE, entity);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                LmsApiResponse<UserSimple> apiResponse = parseResponse(response.getBody(), UserSimple.class);

                assertEquals(patronUser.getEmail(), apiResponse.getData().getEmail());

                User deleted = userRepository.findById(patronUser.getId()).orElse(null);
                assertNull(deleted);
        }

        @Test
        void testDeleteMyUser_whenMyUserDoesNotExist() {
                String fakeToken = createToken("ghostuser@example.com");
                HttpEntity<String> entity = createEntity(null, fakeToken);
                ResponseEntity<String> response = sendRequest("/me", HttpMethod.DELETE, entity);

                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        void testGetUser_whenPatronTriesToReadLibrarian() {
                HttpEntity<String> entity = createEntity(null, patronUser.getEmail());
                ResponseEntity<String> response = sendRequest("/" + librarianUser.getId(), HttpMethod.GET, entity);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testGetUser_whenLibrarianReadsPatron() {
                HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());
                ResponseEntity<String> response = sendRequest("/" + patronUser.getId(), HttpMethod.GET, entity);

                assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        void testGetUser_whenLibrarianReadsAdmin() {
                HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());
                ResponseEntity<String> response = sendRequest("/" + adminUser.getId(), HttpMethod.GET, entity);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testUpdateUser_whenPatronTriesToModifyLibrarian() {
                UserUpdateRequest updateRequest = buildUpdateRequest(librarianUser, "Updated");
                HttpEntity<String> entity = createEntity(updateRequest, patronUser.getEmail());
                ResponseEntity<String> response = sendRequest("/" + librarianUser.getId(), HttpMethod.PUT, entity);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testUpdateUser_whenLibrarianTriesToModifyLibrarian() {
                User librarianUser2 = createUser("huseyinsarsilmaz4@hotmail.com", "ROLE_LIBRARIAN,ROLE_PATRON",
                                "Hüseyin", "Sarsılmaz", "1234567891");
                UserUpdateRequest updateRequest = buildUpdateRequest(librarianUser2, "Updated");
                HttpEntity<String> entity = createEntity(updateRequest, librarianUser.getEmail());
                ResponseEntity<String> response = sendRequest("/" + librarianUser2.getId(), HttpMethod.PUT, entity);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testUpdateUser_whenLibrarianModifiesPatron() throws JsonProcessingException {
                UserUpdateRequest updateRequest = buildUpdateRequest(patronUser, "Updated");
                HttpEntity<String> entity = createEntity(updateRequest, librarianUser.getEmail());
                ResponseEntity<String> response = sendRequest("/" + patronUser.getId(), HttpMethod.PUT, entity);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                LmsApiResponse<UserSimple> apiResponse = parseResponse(response.getBody(), UserSimple.class);

                assertEquals("Updated", apiResponse.getData().getName());
        }

        @Test
        void testDeleteUser_whenPatronTriesToDeleteLibrarian() {
                HttpEntity<String> entity = createEntity(null, patronUser.getEmail());
                ResponseEntity<String> response = sendRequest("/" + librarianUser.getId(), HttpMethod.DELETE, entity);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testDeleteUser_whenLibrarianTriesToDeleteLibrarian() {
                User librarianUser2 = createUser("librarian2@example.com", "ROLE_LIBRARIAN,ROLE_PATRON", "Second",
                                "Librarian", "9876543210");
                HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());
                ResponseEntity<String> response = sendRequest("/" + librarianUser2.getId(), HttpMethod.DELETE, entity);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testDeleteUser_whenLibrarianDeletesPatron() throws JsonProcessingException {
                HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());
                ResponseEntity<String> response = sendRequest("/" + patronUser.getId(), HttpMethod.DELETE, entity);
                assertEquals(HttpStatus.OK, response.getStatusCode());

                LmsApiResponse<UserSimple> apiResponse = parseResponse(response.getBody(), UserSimple.class);

                User deleted = userRepository.findById(apiResponse.getData().getId()).orElse(null);
                assertNull(deleted);
        }

        @Test
        void testDeleteUser_whenAdminDeletesLibrarian() throws JsonProcessingException {
                HttpEntity<String> entity = createEntity(null, adminUser.getEmail());
                ResponseEntity<String> response = sendRequest("/" + librarianUser.getId(), HttpMethod.DELETE, entity);
                assertEquals(HttpStatus.OK, response.getStatusCode());

                LmsApiResponse<UserSimple> apiResponse = parseResponse(response.getBody(), UserSimple.class);

                User deleted = userRepository.findById(apiResponse.getData().getId()).orElse(null);
                assertNull(deleted);
        }

        @Test
        void testReactivateUser_whenMyUserNotExists() {
                HttpEntity<String> entity = createEntity(null, "nonexistent@email.com");
                ResponseEntity<String> response = sendRequest("/" + patronUser.getId() + "/reactivate", HttpMethod.POST,
                                entity);

                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        void testReactivateUser_whenRoleIsNotEnough() {
                HttpEntity<String> entity = createEntity(null, patronUser.getEmail());
                ResponseEntity<String> response = sendRequest("/" + librarianUser.getId() + "/reactivate",
                                HttpMethod.POST,
                                entity);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testReactivateUser_whenReactivatedUserNotExists() {
                HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());
                ResponseEntity<String> response = sendRequest("/99999/reactivate", HttpMethod.POST, entity);

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        void testReactivateUser_whenUserIsAlreadyActive() {
                HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());
                ResponseEntity<String> response = sendRequest("/" + patronUser.getId() + "/reactivate",
                                HttpMethod.POST,
                                entity);

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
                ResponseEntity<String> response = sendRequest("/" + deactivatedUser.getId() + "/reactivate",
                                HttpMethod.POST,
                                entity);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                LmsApiResponse<UserDetailed> apiResponse = parseResponse(response.getBody(), UserDetailed.class);

                assertEquals(deactivatedUser.getEmail(), apiResponse.getData().getEmail());
                assertTrue(apiResponse.getData().getIsActive());
        }

        @Test
        void testPromoteRequest_whenEmailIsInvalid() {
                PromoteRequest req = new PromoteRequest("invalid-email", User.Role.ROLE_LIBRARIAN);
                HttpEntity<String> entity = createEntity(req, librarianUser.getEmail());
                ResponseEntity<String> response = sendRequest("/promote", HttpMethod.POST, entity);

                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        void testPromoteRequest_whenEmailIsEmpty() {
                PromoteRequest req = new PromoteRequest("", User.Role.ROLE_LIBRARIAN);
                HttpEntity<String> entity = createEntity(req, librarianUser.getEmail());
                ResponseEntity<String> response = sendRequest("/promote", HttpMethod.POST, entity);

                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        void testPromoteRequest_whenNewRoleIsNull() {
                PromoteRequest req = new PromoteRequest("user@example.com", null);
                HttpEntity<String> entity = createEntity(req, librarianUser.getEmail());
                ResponseEntity<String> response = sendRequest("/promote", HttpMethod.POST, entity);

                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        void testPromoteRequest_whenValid() {
                PromoteRequest req = new PromoteRequest(patronUser.getEmail(), User.Role.ROLE_LIBRARIAN);
                HttpEntity<String> entity = createEntity(req, adminUser.getEmail());
                ResponseEntity<String> response = sendRequest("/promote", HttpMethod.POST, entity);

                assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        void testUserUpdateRequest_whenEmailIsInvalid() {
                UserUpdateRequest req = new UserUpdateRequest("invalid-email", "John", "Doe", "1234567890");
                HttpEntity<String> entity = createEntity(req, librarianUser.getEmail());
                ResponseEntity<String> response = sendRequest("/me", HttpMethod.PUT, entity);

                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        void testUserUpdateRequest_whenNameIsTooShort() {
                UserUpdateRequest req = new UserUpdateRequest("user@example.com", "J", "Doe", "1234567890");
                HttpEntity<String> entity = createEntity(req, librarianUser.getEmail());
                ResponseEntity<String> response = sendRequest("/me", HttpMethod.PUT, entity);

                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        void testUserUpdateRequest_whenNameHasNonAlphabeticalChars() {
                UserUpdateRequest req = new UserUpdateRequest("user@example.com", "John1", "Doe", "1234567890");
                HttpEntity<String> entity = createEntity(req, librarianUser.getEmail());
                ResponseEntity<String> response = sendRequest("/me", HttpMethod.PUT, entity);

                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        void testUserUpdateRequest_whenSurnameIsTooShort() {
                UserUpdateRequest req = new UserUpdateRequest("user@example.com", "John", "D", "1234567890");
                HttpEntity<String> entity = createEntity(req, librarianUser.getEmail());
                ResponseEntity<String> response = sendRequest("/me", HttpMethod.PUT, entity);

                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        void testUserUpdateRequest_whenPhoneNumberIsInvalid() {
                UserUpdateRequest req = new UserUpdateRequest("user@example.com", "John", "Doe", "123");
                HttpEntity<String> entity = createEntity(req, librarianUser.getEmail());
                ResponseEntity<String> response = sendRequest("/me", HttpMethod.PUT, entity);

                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        void testUserUpdateRequest_whenValid() {
                UserUpdateRequest req = buildUpdateRequest(librarianUser, "newname");
                HttpEntity<String> entity = createEntity(req, librarianUser.getEmail());
                ResponseEntity<String> response = sendRequest("/me", HttpMethod.PUT, entity);

                assertEquals(HttpStatus.OK, response.getStatusCode());
        }

}
