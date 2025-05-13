package com.huseyinsarsilmaz.lms.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.LocalDate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.huseyinsarsilmaz.lms.model.dto.request.BookCreateRequest;
import com.huseyinsarsilmaz.lms.model.dto.request.BookUpdateRequest;
import com.huseyinsarsilmaz.lms.model.dto.response.LmsApiResponse;
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

        @Autowired
        private ObjectMapper objectMapper;

        private final RestTemplate restTemplate = new RestTemplate();

        private String baseUrl;

        private Book savedBook;
        private User librarianUser;
        private User patronUser;

        private static final String EXISTING_ISBN = "1234567890";
        private static final String DUPLICATE_ISBN = "9783161484109";
        private static final String NON_EXISTENT_USER = "notexists@hotmail.com";
        private static final String LIBRARIAN_EMAIL = "huseyinsarsilmaz@hotmail.com";
        private static final String PATRON_EMAIL = "huseyinsarsilmaz2@hotmail.com";

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

        private BookCreateRequest createValidBookRequest(String isbn) {
                return new BookCreateRequest(
                                "How to go to space",
                                "Elon Musk",
                                "How to fire a rocket into space",
                                isbn,
                                LocalDate.now(),
                                Book.Genre.FICTION);
        }

        private BookUpdateRequest createValidUpdateRequest(String isbn) {
                return new BookUpdateRequest(
                                "Updated Title",
                                "Updated Author",
                                "Updated description",
                                isbn,
                                LocalDate.now(),
                                Book.Genre.FICTION);
        }

        private ResponseEntity<String> sendRequest(String path, HttpMethod method, HttpEntity<String> entity) {
                return restTemplate.exchange(baseUrl + path, method, entity, String.class);
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
                userRepository.deleteAll();

                savedBook = bookRepository.save(Book.builder()
                                .isbn(EXISTING_ISBN)
                                .title("How to write code")
                                .author("Hüseyin Sarsılmaz")
                                .isAvailable(true)
                                .publicationDate(LocalDate.now())
                                .genre(Book.Genre.ADVENTURE)
                                .build());

                librarianUser = userRepository.save(User.builder()
                                .email(LIBRARIAN_EMAIL)
                                .password("MTIzNDU2Nzg")
                                .roles("ROLE_LIBRARIAN,ROLE_PATRON")
                                .phoneNumber("1234567891")
                                .name("Hüseyin")
                                .surname("Sarsılmaz")
                                .isActive(true)
                                .build());

                patronUser = userRepository.save(User.builder()
                                .email(PATRON_EMAIL)
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
                ResponseEntity<String> response = sendRequest("/" + savedBook.getId(), HttpMethod.GET, entity);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                LmsApiResponse<BookSimple> apiResponse = objectMapper.readValue(response.getBody(),
                                new TypeReference<>() {
                });

                assertEquals(savedBook.getTitle(), apiResponse.getData().getTitle());
        }

        @Test
        void testGetBook_whenBookDoesNotExist() {
                HttpEntity<String> entity = createEntity(null, patronUser.getEmail());
                ResponseEntity<String> response = sendRequest("/99999", HttpMethod.GET, entity);

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        void testCreateBook_whenUserNotExists() {
                BookCreateRequest req = createValidBookRequest("978-3-16-148410-0");
                HttpEntity<String> entity = createEntity(req, NON_EXISTENT_USER);
                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, entity, String.class);

                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        void testCreateBook_whenUserRoleIsNotLibrarian() {
                BookCreateRequest req = createValidBookRequest("978-3-16-148410-0");
                HttpEntity<String> entity = createEntity(req, patronUser.getEmail());
                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, entity, String.class);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testCreateBook_whenIsbnIsTaken() {
                BookCreateRequest req = createValidBookRequest(EXISTING_ISBN);
                HttpEntity<String> entity = createEntity(req, librarianUser.getEmail());
                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, entity, String.class);

                assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        }

        @Test
        void testCreateBook_whenRequestIsValid() throws JsonProcessingException {
                BookCreateRequest req = createValidBookRequest("9783161484100");
                HttpEntity<String> entity = createEntity(req, librarianUser.getEmail());
                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, entity, String.class);

                assertEquals(HttpStatus.CREATED, response.getStatusCode());

                LmsApiResponse<BookSimple> apiResponse = objectMapper.readValue(response.getBody(),
                                new TypeReference<>() {
                });

                assertEquals(req.getTitle(), apiResponse.getData().getTitle());
                assertEquals(req.getIsbn(), apiResponse.getData().getIsbn());
        }

        @Test
        void testUpdateBook_whenUserNotExists() {
                BookUpdateRequest req = createValidUpdateRequest(DUPLICATE_ISBN);
                HttpEntity<String> entity = createEntity(req, NON_EXISTENT_USER);
                ResponseEntity<String> response = sendRequest("/" + savedBook.getId(), HttpMethod.PUT, entity);

                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        void testUpdateBook_whenUserRoleIsNotLibrarian() {
                BookUpdateRequest req = createValidUpdateRequest(DUPLICATE_ISBN);
                HttpEntity<String> entity = createEntity(req, patronUser.getEmail());
                ResponseEntity<String> response = sendRequest("/" + savedBook.getId(), HttpMethod.PUT, entity);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testUpdateBook_whenBookDoesNotExist() {
                BookUpdateRequest req = createValidUpdateRequest(DUPLICATE_ISBN);
                HttpEntity<String> entity = createEntity(req, librarianUser.getEmail());
                ResponseEntity<String> response = sendRequest("/99999", HttpMethod.PUT, entity);

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        void testUpdateBook_whenIsbnIsTaken() {
                bookRepository.save(Book.builder()
                                .isbn(DUPLICATE_ISBN)
                                .title("Another Book")
                                .author("Author")
                                .isAvailable(true)
                                .publicationDate(LocalDate.now())
                                .genre(Book.Genre.ADVENTURE)
                                .build());

                BookUpdateRequest req = createValidUpdateRequest(DUPLICATE_ISBN);
                HttpEntity<String> entity = createEntity(req, librarianUser.getEmail());
                ResponseEntity<String> response = sendRequest("/" + savedBook.getId(), HttpMethod.PUT, entity);

                assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        }

        @Test
        void testUpdateBook_whenRequestIsValid() throws JsonProcessingException {
                BookUpdateRequest req = createValidUpdateRequest(savedBook.getIsbn());
                HttpEntity<String> entity = createEntity(req, librarianUser.getEmail());
                ResponseEntity<String> response = sendRequest("/" + savedBook.getId(), HttpMethod.PUT, entity);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                LmsApiResponse<BookSimple> apiResponse = objectMapper.readValue(response.getBody(),
                                new TypeReference<>() {
                });

                assertEquals(req.getTitle(), apiResponse.getData().getTitle());
                assertEquals(req.getIsbn(), apiResponse.getData().getIsbn());
        }

        @Test
        void testDeleteBook_whenUserNotExists() {
                HttpEntity<String> entity = createEntity(null, NON_EXISTENT_USER);
                ResponseEntity<String> response = sendRequest("/" + savedBook.getId(), HttpMethod.DELETE, entity);

                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        void testDeleteBook_whenUserRoleIsNotLibrarian() {
                HttpEntity<String> entity = createEntity(null, patronUser.getEmail());
                ResponseEntity<String> response = sendRequest("/" + savedBook.getId(), HttpMethod.DELETE, entity);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testDeleteBook_whenBookDoesNotExist() {
                HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());
                ResponseEntity<String> response = sendRequest("/99999", HttpMethod.DELETE, entity);

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        void testDeleteBook_whenRequestIsValid() {
                HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());
                ResponseEntity<String> response = sendRequest("/" + savedBook.getId(), HttpMethod.DELETE, entity);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                Book deleted = bookRepository.findById(savedBook.getId()).orElse(null);
                assertNull(deleted);
        }

        @Test
        void testCreateBook_whenTitleIsBlank() {
                BookCreateRequest req = createValidBookRequest("978-3-16-148410-1");
                req.setTitle(" ");
                HttpEntity<String> entity = createEntity(req, librarianUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, entity, String.class);

                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        void testCreateBook_whenDescriptionTooShort() {
                BookCreateRequest req = createValidBookRequest("978-3-16-148410-2");
                req.setDescription("Short");
                HttpEntity<String> entity = createEntity(req, librarianUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, entity, String.class);

                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        void testCreateBook_whenIsbnInvalid() {
                BookCreateRequest req = createValidBookRequest("INVALID_ISBN");
                HttpEntity<String> entity = createEntity(req, librarianUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, entity, String.class);

                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        void testCreateBook_whenPublicationDateIsInFuture() {
                BookCreateRequest req = createValidBookRequest("978-3-16-148410-3");
                req.setPublicationDate(LocalDate.now().plusDays(10));
                HttpEntity<String> entity = createEntity(req, librarianUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, entity, String.class);

                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        void testUpdateBook_whenTitleTooLong() {
                String longTitle = "T".repeat(300);
                BookUpdateRequest req = createValidUpdateRequest(savedBook.getIsbn());
                req.setTitle(longTitle);
                HttpEntity<String> entity = createEntity(req, librarianUser.getEmail());
                ResponseEntity<String> response = sendRequest("/" + savedBook.getId(), HttpMethod.PUT, entity);

                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        void testUpdateBook_whenGenreIsNull() {
                BookUpdateRequest req = createValidUpdateRequest(savedBook.getIsbn());
                req.setGenre(null);
                HttpEntity<String> entity = createEntity(req, librarianUser.getEmail());
                ResponseEntity<String> response = sendRequest("/" + savedBook.getId(), HttpMethod.PUT, entity);

                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        void testUpdateBook_whenAuthorIsEmpty() {
                BookUpdateRequest req = createValidUpdateRequest(savedBook.getIsbn());
                req.setAuthor("");
                HttpEntity<String> entity = createEntity(req, librarianUser.getEmail());
                ResponseEntity<String> response = sendRequest("/" + savedBook.getId(), HttpMethod.PUT, entity);

                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

}
