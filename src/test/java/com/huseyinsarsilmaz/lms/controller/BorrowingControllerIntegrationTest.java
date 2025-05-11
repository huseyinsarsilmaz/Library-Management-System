package com.huseyinsarsilmaz.lms.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.huseyinsarsilmaz.lms.model.dto.request.BorrowRequest;
import com.huseyinsarsilmaz.lms.model.dto.response.ApiResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.BorrowingDetailed;
import com.huseyinsarsilmaz.lms.model.dto.response.BorrowingHistory;
import com.huseyinsarsilmaz.lms.model.dto.response.BorrowingSimple;
import com.huseyinsarsilmaz.lms.model.dto.response.PagedResponse;
import com.huseyinsarsilmaz.lms.model.entity.Book;
import com.huseyinsarsilmaz.lms.model.entity.Borrowing;
import com.huseyinsarsilmaz.lms.model.entity.User;
import com.huseyinsarsilmaz.lms.repository.BookRepository;
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

import java.io.IOException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BorrowingControllerIntegrationTest {

        @LocalServerPort
        private int port;

        @Autowired
        private JwtService jwtService;

        @Autowired
        private BookRepository bookRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private BorrowingRepository borrowingRepository;

        private final RestTemplate restTemplate = new RestTemplate();
        private final ObjectMapper objectMapper = new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        private String baseUrl;

        private User patronUser;
        private User librarianUser;
        private Book borrowedBook;
        private Borrowing borrowing;

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
                baseUrl = "http://localhost:" + port + "/api/borrowings";

                restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
                        @Override
                        public boolean hasError(@NonNull ClientHttpResponse response) throws IOException {
                                return false;
                        }
                });

                borrowingRepository.deleteAll();
                bookRepository.deleteAll();
                userRepository.deleteAll();

                patronUser = userRepository.save(User.builder()
                                .email("patron@example.com")
                                .password("password")
                                .roles("ROLE_PATRON")
                                .phoneNumber("1111111111")
                                .name("Patron")
                                .surname("User")
                                .isActive(true)
                                .build());

                librarianUser = userRepository.save(User.builder()
                                .email("librarian@example.com")
                                .password("password")
                                .roles("ROLE_LIBRARIAN")
                                .phoneNumber("2222222222")
                                .name("Librarian")
                                .surname("User")
                                .isActive(true)
                                .build());

                borrowedBook = bookRepository.save(Book.builder()
                                .isbn("1234567890123")
                                .title("Book Title")
                                .author("Author Name")
                                .isAvailable(false)
                                .build());

                borrowing = borrowingRepository.save(Borrowing.builder()
                                .book(borrowedBook)
                                .borrower(patronUser)
                                .borrowDate(LocalDate.now().minusDays(5))
                                .dueDate(LocalDate.now().plusDays(5))
                                .status(Borrowing.Status.BORROWED)
                                .returnDate(null)
                                .build());
        }

        @Test
        void testGetMyBorrowingHistory_whenUserNotExists() {
                HttpEntity<String> entity = createEntity(null, "nonexistent@hotmail.com");

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/my",
                                HttpMethod.GET,
                                entity,
                                String.class);

                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        void testGetMyBorrowingHistory_whenUserExists() throws JsonProcessingException {
                HttpEntity<String> entity = createEntity(null, patronUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/my",
                                HttpMethod.GET,
                                entity,
                                String.class);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                ApiResponse<BorrowingHistory> apiResponse = objectMapper.readValue(
                                response.getBody(),
                                new TypeReference<ApiResponse<BorrowingHistory>>() {
                                });

                assertNotNull(apiResponse.getData());
                assertEquals(1, apiResponse.getData().getActive().size());
                assertEquals(borrowedBook.getTitle(), apiResponse.getData().getActive().get(0).getBook().getTitle());
        }

        @Test
        void testGetBorrowingHistory_whenMyUserNotExists() {
                HttpEntity<String> entity = createEntity(null, "nonexistent@example.com");

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/user/" + patronUser.getId(),
                                HttpMethod.GET,
                                entity,
                                String.class);

                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        void testGetBorrowingHistory_whenUserRoleIsNotLibrarian() {
                HttpEntity<String> entity = createEntity(null, patronUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/user/" + patronUser.getId(),
                                HttpMethod.GET,
                                entity,
                                String.class);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testGetBorrowingHistory_whenBorrowedUserNotExists() {
                HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/user/99999",
                                HttpMethod.GET,
                                entity,
                                String.class);

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        void testGetBorrowingHistory_whenRequestIsValid() throws JsonProcessingException {
                HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/user/" + patronUser.getId(),
                                HttpMethod.GET,
                                entity,
                                String.class);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                ApiResponse<BorrowingHistory> apiResponse = objectMapper.readValue(
                                response.getBody(),
                                new TypeReference<ApiResponse<BorrowingHistory>>() {
                                });

                assertNotNull(apiResponse.getData());
                assertEquals(1, apiResponse.getData().getActive().size());
                assertEquals(borrowedBook.getTitle(), apiResponse.getData().getActive().get(0).getBook().getTitle());
        }

        @Test
        void testGetBorrowingReport_whenUserNotExists() {
                HttpEntity<String> entity = createEntity(null, "nonexistent@example.com");

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/report?borrowerId=" + patronUser.getId(),
                                HttpMethod.GET,
                                entity,
                                String.class);

                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        void testGetBorrowingReport_whenUserRoleIsNotLibrarian() {
                HttpEntity<String> entity = createEntity(null, patronUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/report?borrowerId=" + patronUser.getId(),
                                HttpMethod.GET,
                                entity,
                                String.class);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testGetBorrowingReport_whenBorrowerIdGivenButUserNotExists() {
                HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/report?borrowerId=99999",
                                HttpMethod.GET,
                                entity,
                                String.class);

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        void testGetBorrowingReport_whenBorrowerIdGivenAndValid() throws JsonProcessingException {
                borrowing.setStatus(Borrowing.Status.RETURNED_OVERDUE);
                borrowing.setReturnDate(LocalDate.now());

                borrowingRepository.save(borrowing);

                HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/report?borrowerId=" + patronUser.getId(),
                                HttpMethod.GET,
                                entity,
                                String.class);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                ApiResponse<PagedResponse<BorrowingDetailed>> apiResponse = objectMapper.readValue(
                                response.getBody(),
                                new TypeReference<ApiResponse<PagedResponse<BorrowingDetailed>>>() {
                                });

                assertNotNull(apiResponse.getData());
                assertEquals(1, apiResponse.getData().getItems().size());
                assertEquals(borrowedBook.getTitle(), apiResponse.getData().getItems().get(0).getBook().getTitle());
        }

        @Test
        void testGetBorrowingReport_whenBorrowerIdNotGiven() throws JsonProcessingException {
                borrowing.setStatus(Borrowing.Status.RETURNED_OVERDUE);
                borrowing.setReturnDate(LocalDate.now());

                borrowingRepository.save(borrowing);

                Book newBook = bookRepository.save(Book.builder()
                                .isbn("1234567890124")
                                .title("New Book")
                                .author("New Authjor")
                                .isAvailable(false)
                                .build());

                borrowingRepository.save(Borrowing.builder()
                                .book(newBook)
                                .borrower(librarianUser)
                                .borrowDate(LocalDate.now().minusDays(5))
                                .dueDate(LocalDate.now().plusDays(5))
                                .returnDate(LocalDate.now())
                                .status(Borrowing.Status.RETURNED_TIMELY)
                                .returnDate(null)
                                .build());

                HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());

                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/report",
                                HttpMethod.GET,
                                entity,
                                String.class);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                ApiResponse<PagedResponse<BorrowingDetailed>> apiResponse = objectMapper.readValue(
                                response.getBody(),
                                new TypeReference<ApiResponse<PagedResponse<BorrowingDetailed>>>() {
                                });

                assertNotNull(apiResponse.getData());
                assertEquals(2, apiResponse.getData().getItems().size());
                assertEquals(borrowedBook.getTitle(), apiResponse.getData().getItems().get(0).getBook().getTitle());
                assertEquals(newBook.getTitle(), apiResponse.getData().getItems().get(1).getBook().getTitle());
        }

        @Test
        void testCreateBorrowing_whenUserNotExists() throws JsonProcessingException {
                BorrowRequest req = new BorrowRequest(null, borrowedBook.getId());

                HttpEntity<String> entity = createEntity(req, "notexists@hotmail.com");

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, entity, String.class);

                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        void testCreateBorrowing_whenBorrowerIdIsGivenButRoleIsNotEnough() {
                BorrowRequest req = new BorrowRequest(patronUser.getId(), borrowedBook.getId());

                HttpEntity<String> entity = createEntity(req, patronUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, entity, String.class);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testCreateBorrowing_whenBorrowerIsNotActive() {
                patronUser.setIsActive(false);
                userRepository.save(patronUser);

                BorrowRequest req = new BorrowRequest(null, borrowedBook.getId());
                HttpEntity<String> entity = createEntity(req, patronUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, entity, String.class);
                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testCreateBorrowing_whenUserHasOverdueBorrowings() {
                borrowing.setDueDate(LocalDate.now().minusDays(10));
                borrowing.setStatus(Borrowing.Status.OVERDUE);
                borrowingRepository.save(borrowing);

                BorrowRequest req = new BorrowRequest(null, borrowedBook.getId());
                HttpEntity<String> entity = createEntity(req, patronUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, entity, String.class);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testCreateBorrowing_whenUserAlreadyBorrowedSameBook() {
                BorrowRequest req = new BorrowRequest(null, borrowedBook.getId());
                HttpEntity<String> entity = createEntity(req, patronUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, entity, String.class);

                assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        }

        @Test
        void testCreateBorrowing_whenBookNotAvailable() {
                Book newBook = bookRepository.save(Book.builder()
                                .isbn("1234567890124")
                                .title("Book Title")
                                .author("Author Name")
                                .isAvailable(false)
                                .build());

                BorrowRequest req = new BorrowRequest(null, newBook.getId());
                HttpEntity<String> entity = createEntity(req, patronUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, entity, String.class);

                assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        }

        @Test
        void testCreateBorrowing_whenRequestIsValidSameUser() throws JsonProcessingException {
                Book newBook = bookRepository.save(Book.builder()
                                .isbn("1234567890124")
                                .title("Book Title")
                                .author("Author Name")
                                .isAvailable(true)
                                .build());
                bookRepository.save(newBook);

                BorrowRequest req = new BorrowRequest(null, newBook.getId());
                HttpEntity<String> entity = createEntity(req, patronUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, entity, String.class);

                assertEquals(HttpStatus.CREATED, response.getStatusCode());

                ApiResponse<BorrowingSimple> apiResponse = objectMapper.readValue(
                                response.getBody(),
                                new TypeReference<ApiResponse<BorrowingSimple>>() {
                                });

                assertNotNull(apiResponse.getData());
                assertEquals(newBook.getTitle(), apiResponse.getData().getBook().getTitle());
        }

        @Test
        void testCreateBorrowing_whenRequestIsValidOtherUser() throws JsonProcessingException {
                Book newBook = bookRepository.save(Book.builder()
                                .isbn("1234567890124")
                                .title("Book Title")
                                .author("Author Name")
                                .isAvailable(true)
                                .build());
                bookRepository.save(newBook);

                BorrowRequest req = new BorrowRequest(patronUser.getId(), newBook.getId());
                HttpEntity<String> entity = createEntity(req, librarianUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, entity, String.class);

                assertEquals(HttpStatus.CREATED, response.getStatusCode());

                ApiResponse<BorrowingSimple> apiResponse = objectMapper.readValue(
                                response.getBody(),
                                new TypeReference<ApiResponse<BorrowingSimple>>() {
                                });

                assertNotNull(apiResponse.getData());
                assertEquals(newBook.getTitle(), apiResponse.getData().getBook().getTitle());
        }

        @Test
        void testReturnBorrowing_whenUserNotExists() {
                HttpEntity<String> entity = createEntity(null, "notexists@hotmail.com");

                ResponseEntity<String> response = restTemplate.postForEntity(
                                baseUrl + "/" + borrowing.getId() + "/return",
                                entity,
                                String.class);

                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        void testReturnBorrowing_whenBorrowingNotExists() {
                HttpEntity<String> entity = createEntity(null, patronUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(
                                baseUrl + "/999999/return",
                                entity,
                                String.class);

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        void testReturnBorrowing_whenBorrowingIsNotOwnedByUser() {
                HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(
                                baseUrl + "/" + borrowing.getId() + "/return",
                                entity,
                                String.class);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testReturnBorrowing_whenBorrowingIsNotReturnable() {
                borrowing.setStatus(Borrowing.Status.RETURNED_TIMELY);
                borrowingRepository.save(borrowing);

                HttpEntity<String> entity = createEntity(null, patronUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(
                                baseUrl + "/" + borrowing.getId() + "/return",
                                entity,
                                String.class);

                assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        }

        @Test
        void testReturnBorrowing_whenRequestIsValid() throws JsonProcessingException {
                borrowing.setStatus(Borrowing.Status.BORROWED);
                borrowingRepository.save(borrowing);

                HttpEntity<String> entity = createEntity(null, patronUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(
                                baseUrl + "/" + borrowing.getId() + "/return",
                                entity,
                                String.class);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                ApiResponse<BorrowingDetailed> apiResponse = objectMapper.readValue(
                                response.getBody(),
                                new TypeReference<ApiResponse<BorrowingDetailed>>() {
                                });

                assertNotNull(apiResponse.getData());
                assertEquals(borrowedBook.getTitle(), apiResponse.getData().getBook().getTitle());
                assertEquals(Borrowing.Status.RETURNED_TIMELY, apiResponse.getData().getStatus());
        }

        @Test
        void testExcuseBorrowing_whenUserNotExists() {
                HttpEntity<String> entity = createEntity(null, "notexists@hotmail.com");

                ResponseEntity<String> response = restTemplate.postForEntity(
                                baseUrl + "/" + +borrowing.getId() + "/excuse",
                                entity,
                                String.class);

                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        void testExcuseBorrowing_whenRoleIsNotEnough() {
                HttpEntity<String> entity = createEntity(null, patronUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(
                                baseUrl + "/" + +borrowing.getId() + "/excuse",
                                entity,
                                String.class);
                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        void testExcuseBorrowing_whenBorrowingNotExists() {
                HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(
                                baseUrl + "/" + +Long.MAX_VALUE + "/excuse",
                                entity,
                                String.class);

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        void testExcuseBorrowing_whenBorrowingIsNotExcusable() {

                HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(
                                baseUrl + "/" + +borrowing.getId() + "/excuse",
                                entity,
                                String.class);

                assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        }

        @Test
        void testExcuseBorrowing_whenRequestIsValid() throws JsonProcessingException {
                borrowing.setStatus(Borrowing.Status.RETURNED_OVERDUE);
                borrowingRepository.save(borrowing);

                HttpEntity<String> entity = createEntity(null, librarianUser.getEmail());

                ResponseEntity<String> response = restTemplate.postForEntity(
                                baseUrl + "/" + +borrowing.getId() + "/excuse",
                                entity,
                                String.class);

                assertEquals(HttpStatus.OK, response.getStatusCode());

                ApiResponse<BorrowingDetailed> apiResponse = objectMapper.readValue(
                                response.getBody(),
                                new TypeReference<ApiResponse<BorrowingDetailed>>() {
                                });

                assertNotNull(apiResponse.getData());
                assertEquals(borrowing.getId(), apiResponse.getData().getId());
                assertEquals(Borrowing.Status.RETURNED_EXCUSED, apiResponse.getData().getStatus());
        }

}
