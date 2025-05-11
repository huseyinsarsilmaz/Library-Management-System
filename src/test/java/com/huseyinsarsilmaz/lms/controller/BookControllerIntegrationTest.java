package com.huseyinsarsilmaz.lms.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.LocalDate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.huseyinsarsilmaz.lms.model.dto.request.BookCreateRequest;
import com.huseyinsarsilmaz.lms.model.dto.request.BookUpdateRequest;
import com.huseyinsarsilmaz.lms.model.dto.response.ApiResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.BookSimple;

import com.huseyinsarsilmaz.lms.model.entity.Book;
import com.huseyinsarsilmaz.lms.model.entity.User;
import com.huseyinsarsilmaz.lms.repository.BookRepository;
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
class BookControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private String baseUrl;

    private Book savedBook;
    private User librarianUser;
    private User patronUser;

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
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
        baseUrl = "http://localhost:" + port + "/api/books";

        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(@NonNull ClientHttpResponse response) throws IOException {
                return false;
            }
        });

        bookRepository.deleteAll();

        savedBook = bookRepository.save(Book.builder()
                .isbn("1234567890")
                .title("How to write code")
                .author("Hüseyin Sarsılmaz")
                .isAvailable(true)
                .build());

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
    }

    @Test
    void testGetBook_whenBookExists() throws JsonProcessingException {
        HttpEntity<String> entity = createEntity(null, patronUser.getEmail());

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + savedBook.getId(),
                HttpMethod.GET,
                entity,
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<BookSimple> apiResponse = objectMapper.readValue(
                response.getBody(),
                new TypeReference<ApiResponse<BookSimple>>() {
                });

        assertEquals(savedBook.getTitle(), apiResponse.getData().getTitle());
    }

    @Test
    void testGetBook_whenBookDoesNotExist() {
        HttpEntity<String> entity = createEntity(null, patronUser.getEmail());

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/99999",
                HttpMethod.GET,
                entity,
                String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testCreateBook_whenUserNotExists() throws JsonProcessingException {
        BookCreateRequest req = new BookCreateRequest("How to go to space",
                "Elon Musk",
                "How to fire a rocket into space",
                "978-3-16-148410-0",
                LocalDate.now(),
                Book.Genre.FICTION);
        HttpEntity<String> entity = createEntity(
                req,
                "notexists@hotmail.com");

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, entity, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testCreateBook_whenUserRoleIsNotLibrarian() {
        BookCreateRequest req = new BookCreateRequest("How to go to space",
                "Elon Musk",
                "How to fire a rocket into space",
                "978-3-16-148410-0",
                LocalDate.now(),
                Book.Genre.FICTION);
        HttpEntity<String> entity = createEntity(
                req,
                patronUser.getEmail());

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, entity, String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testCreateBook_whenIsbnIsTaken() {
        BookCreateRequest req = new BookCreateRequest("How to go to space",
                "Elon Musk",
                "How to fire a rocket into space",
                savedBook.getIsbn(),
                LocalDate.now(),
                Book.Genre.FICTION);
        HttpEntity<String> entity = createEntity(
                req,
                librarianUser.getEmail());

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, entity, String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void testCreateBook_whenRequestIsValid() throws JsonProcessingException {
        BookCreateRequest req = new BookCreateRequest("How to go to space",
                "Elon Musk",
                "How to fire a rocket into space",
                "978-3-16-148410-0",
                LocalDate.now(),
                Book.Genre.FICTION);
        HttpEntity<String> entity = createEntity(
                req,
                librarianUser.getEmail());

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, entity, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        ApiResponse<BookSimple> apiResponse = objectMapper.readValue(
                response.getBody(),
                new TypeReference<ApiResponse<BookSimple>>() {
                });

        assertEquals(req.getTitle(), apiResponse.getData().getTitle());
        assertEquals(req.getIsbn(), apiResponse.getData().getIsbn());
    }

    @Test
    void testUpdateBook_whenUserNotExists() {
        BookUpdateRequest req = new BookUpdateRequest(
                "Updated Title",
                "Updated Author",
                "Updated description",
                "978-3-16-148410-9",
                LocalDate.now(),
                Book.Genre.FICTION);

        HttpEntity<String> entity = createEntity(req, "nonexistentuser@hotmail.com");

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + savedBook.getId(),
                HttpMethod.PUT,
                entity,
                String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testUpdateBook_whenUserRoleIsNotLibrarian() {
        BookUpdateRequest req = new BookUpdateRequest(
                "Updated Title",
                "Updated Author",
                "Updated description",
                "978-3-16-148410-9",
                LocalDate.now(),
                Book.Genre.FICTION);

        HttpEntity<String> entity = createEntity(req, patronUser.getEmail());

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + savedBook.getId(),
                HttpMethod.PUT,
                entity,
                String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testUpdateBook_whenBookDoesNotExist() {
        BookUpdateRequest req = new BookUpdateRequest(
                "Updated Title",
                "Updated Author",
                "Updated description",
                "978-3-16-148410-9",
                LocalDate.now(),
                Book.Genre.FICTION);

        HttpEntity<String> entity = createEntity(req, librarianUser.getEmail());

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/99999",
                HttpMethod.PUT,
                entity,
                String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUpdateBook_whenIsbnIsTaken() {
        Book otherBook = bookRepository.save(Book.builder()
                .isbn("978-3-16-148410-9")
                .title("Another Book")
                .author("Author")
                .isAvailable(true)
                .build());

        BookUpdateRequest req = new BookUpdateRequest(
                "Updated Title",
                "Updated Author",
                "Updated description",
                otherBook.getIsbn(),
                LocalDate.now(),
                Book.Genre.FICTION);

        HttpEntity<String> entity = createEntity(req, librarianUser.getEmail());

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + savedBook.getId(),
                HttpMethod.PUT,
                entity,
                String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void testUpdateBook_whenRequestIsValid() throws JsonProcessingException {
        BookUpdateRequest req = new BookUpdateRequest(
                "Updated Title",
                "Updated Author",
                "Updated description",
                savedBook.getIsbn(),
                LocalDate.now(),
                Book.Genre.FICTION);

        HttpEntity<String> entity = createEntity(req, librarianUser.getEmail());

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + savedBook.getId(),
                HttpMethod.PUT,
                entity,
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<BookSimple> apiResponse = objectMapper.readValue(
                response.getBody(),
                new TypeReference<ApiResponse<BookSimple>>() {
                });

        assertEquals(req.getTitle(), apiResponse.getData().getTitle());
        assertEquals(req.getIsbn(), apiResponse.getData().getIsbn());
    }

    @Test
    void testDeleteBook_whenUserNotExists() {
        HttpEntity<String> entity = createEntity(null, "nonexistentuser@hotmail.com");

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + savedBook.getId(),
                HttpMethod.DELETE,
                entity,
                String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testDeleteBook_whenUserRoleIsNotLibrarian() {
        HttpEntity<String> entity = createEntity(null, patronUser.getEmail());

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + savedBook.getId(),
                HttpMethod.DELETE,
                entity,
                String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testDeleteBook_whenBookDoesNotExist() {
        HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/99999",
                HttpMethod.DELETE,
                entity,
                String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteBook_whenRequestIsValid() throws JsonProcessingException {
        HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + savedBook.getId(),
                HttpMethod.DELETE,
                entity,
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Book deleted = bookRepository.findById(savedBook.getId()).orElse(null);
        assertNull(deleted);

    }

}
