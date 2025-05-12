package com.huseyinsarsilmaz.lms.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.huseyinsarsilmaz.lms.exception.AlreadyExistsException;
import com.huseyinsarsilmaz.lms.exception.NotAvailableException;
import com.huseyinsarsilmaz.lms.exception.NotFoundException;
import com.huseyinsarsilmaz.lms.model.dto.request.BookCreateRequest;
import com.huseyinsarsilmaz.lms.model.dto.request.BookUpdateRequest;
import com.huseyinsarsilmaz.lms.model.entity.Book;
import com.huseyinsarsilmaz.lms.model.mapper.BookMapper;
import com.huseyinsarsilmaz.lms.repository.BookRepository;

import com.huseyinsarsilmaz.lms.service.impl.BookServiceImpl;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book book1;
    private Book book2;

    private static Stream<Arguments> provideSearchParams() {
        return Stream.of(
                Arguments.of(Book.SearchType.TITLE, "code", (Function<Book, ?>) Book::getTitle),
                Arguments.of(Book.SearchType.AUTHOR, "Sarsılmaz", (Function<Book, ?>) Book::getAuthor),
                Arguments.of(Book.SearchType.ISBN, "123", (Function<Book, ?>) Book::getIsbn),
                Arguments.of(Book.SearchType.GENRE, "poetry", (Function<Book, ?>) Book::getGenre));
    }

    @BeforeEach
    public void setUp() {
        book1 = createBook(1L, "How to write code", "Hüseyin Sarsılmaz", "Best practices for writing Java code.",
                "978-0134685991", LocalDate.of(2017, 6, 17), Book.Genre.BIOGRAPHY);
        book2 = createBook(2L, "Jumping Jacks", "Jack the Jumper", "Jumping jack exercises", "123-0134685992",
                LocalDate.of(2021, 8, 17), Book.Genre.POETRY);

    }

    private Book createBook(Long id, String title, String author, String description, String isbn,
            LocalDate publicationDate, Book.Genre genre) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setAuthor(author);
        book.setDescription(description);
        book.setIsbn(isbn);
        book.setPublicationDate(publicationDate);
        book.setGenre(genre);
        book.setIsAvailable(true);
        return book;
    }

    private BookCreateRequest createBookCreateRequest() {
        BookCreateRequest req = new BookCreateRequest();
        req.setTitle("How to write code");
        req.setAuthor("Hüseyin Sarsılmaz");
        req.setDescription("Best practices for writing Java code.");
        req.setIsbn("978-0134685991");
        req.setPublicationDate(LocalDate.of(2017, 6, 17));
        req.setGenre(Book.Genre.BIOGRAPHY);
        return req;
    }

    @Test
    public void testCreateBook_shouldReturnCreatedBookWhenRequestIsValid() {
        BookCreateRequest req = createBookCreateRequest();
        when(bookRepository.save(eq(book1))).thenReturn(book1);
        when(bookMapper.toEntity(any(BookCreateRequest.class))).thenReturn(book1);

        Book createdBook = bookService.create(req);

        assertAll("Create Book",
                () -> assertNotNull(createdBook),
                () -> assertEquals(req.getTitle(), createdBook.getTitle()),
                () -> assertEquals(req.getAuthor(), createdBook.getAuthor()),
                () -> assertEquals(req.getIsbn(), createdBook.getIsbn()));
    }

    @Test
    public void testIsIsbnTaken_shouldThrowExceptionWhenIsbnExists() {
        when(bookRepository.findByIsbn(eq("978-0134685991"))).thenReturn(Optional.of(book1));

        assertThrows(AlreadyExistsException.class, () -> bookService.isIsbnTaken("978-0134685991"));
    }

    @Test
    public void testIsIsbnTaken_shouldNotThrowWhenIsbnDoesNotExist() {
        when(bookRepository.findByIsbn(eq("978-0134686000"))).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> bookService.isIsbnTaken("978-0134686000"));
    }

    @Test
    public void testGetById_shouldReturnBookWhenBookExists() {
        when(bookRepository.findById(eq(1L))).thenReturn(Optional.of(book1));

        Book result = bookService.getById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void testGetById_shouldThrowNotFoundExceptionWhenBookNotFound() {
        when(bookRepository.findById(eq(999L))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.getById(999L));
    }

    @Test
    public void testUpdateBook_shouldReturnUpdatedBookWhenRequestIsValid() {
        BookUpdateRequest req = new BookUpdateRequest();
        req.setTitle("Effective Java (3rd Edition)");
        req.setAuthor("Joshua Bloch");
        req.setDescription("Updated best practices for writing Java code.");
        req.setIsbn("978-0134685991");
        req.setPublicationDate(LocalDate.of(2018, 1, 1));
        req.setGenre(Book.Genre.BIOGRAPHY);

        doAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            BookUpdateRequest request = invocation.getArgument(1);
            book.setTitle(request.getTitle());
            book.setAuthor(request.getAuthor());
            book.setDescription(request.getDescription());
            book.setIsbn(request.getIsbn());
            book.setPublicationDate(request.getPublicationDate());
            book.setGenre(request.getGenre());
            return null;
        }).when(bookMapper).updateEntity(eq(book1), eq(req));

        when(bookRepository.save(eq(book1))).thenAnswer(invocation -> invocation.getArgument(0));

        Book updatedBook = bookService.update(book1, req);

        assertAll("Update Book",
                () -> assertEquals(req.getTitle(), updatedBook.getTitle()),
                () -> assertEquals(req.getAuthor(), updatedBook.getAuthor()),
                () -> assertEquals(req.getIsbn(), updatedBook.getIsbn()),
                () -> assertEquals(book1.getId(), updatedBook.getId()));
    }

    @Test
    public void testDeleteBook_shouldNotThrowExceptionWhenBookIsDeleted() {
        doNothing().when(bookRepository).delete(eq(book1));

        assertDoesNotThrow(() -> bookService.delete(book1));
        verify(bookRepository).delete(book1);
    }

    @ParameterizedTest
    @MethodSource("provideSearchParams")
    public void testSearchBooks_shouldReturnBookWhenSearchCriteriaMatches(Book.SearchType type, String query,
            Function<Book, ?> fieldExtractor) {
        Book expectedBook = type == Book.SearchType.ISBN ? book2 : book1;
        Page<Book> expectedPage = new PageImpl<>(List.of(expectedBook));

        setUpSearchMocks(type, query, expectedPage);

        Page<Book> result = bookService.searchBooks(type, query, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(fieldExtractor.apply(expectedBook), fieldExtractor.apply(result.getContent().get(0)));
    }

    private void setUpSearchMocks(Book.SearchType type, String query, Page<Book> expectedPage) {
        switch (type) {
            case TITLE:
                when(bookRepository.findByTitleContainingIgnoreCase(eq(query), any(Pageable.class)))
                        .thenReturn(expectedPage);
                break;
            case AUTHOR:
                when(bookRepository.findByAuthorContainingIgnoreCase(eq(query), any(Pageable.class)))
                        .thenReturn(expectedPage);
                break;
            case ISBN:
                when(bookRepository.findByIsbnContainingIgnoreCase(eq(query), any(Pageable.class)))
                        .thenReturn(expectedPage);
                break;
            case GENRE:
                when(bookRepository.findByGenre(eq(Book.Genre.POETRY), any(Pageable.class)))
                        .thenReturn(expectedPage);
                break;
        }
    }

    @Test
    public void testSearchBooks_shouldThrowNotFoundExceptionWhenInvalidGenre() {
        assertThrows(NotFoundException.class,
                () -> bookService.searchBooks(Book.SearchType.GENRE, "WRONG-genre", Pageable.unpaged()));
    }

    @Test
    public void testChangeAvailability_shouldReturnUpdatedBookWhenAvailabilityChanges() {
        book1.setIsAvailable(false);
        when(bookRepository.save(eq(book1))).thenAnswer(invocation -> invocation.getArgument(0));

        Book updatedBook = bookService.updateAvailability(book1, true);

        assertTrue(updatedBook.getIsAvailable());
        verify(bookRepository).save(book1);
    }

    @Test
    public void testChangeAvailability_shouldNotSaveWhenAvailabilityDoesNotChange() {
        Book updatedBook = bookService.updateAvailability(book1, true);

        assertTrue(updatedBook.getIsAvailable());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    public void testCheckAvailability_shouldNotThrowExceptionWhenBookIsAvailable() {
        assertDoesNotThrow(() -> bookService.checkAvailability(book1));
    }

    @Test
    public void testCheckAvailability_shouldThrowNotAvailableExceptionWhenBookIsNotAvailable() {
        book1.setIsAvailable(false);

        assertThrows(NotAvailableException.class, () -> bookService.checkAvailability(book1));
    }
}
