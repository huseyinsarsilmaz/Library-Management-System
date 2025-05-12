package com.huseyinsarsilmaz.lms.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
import com.huseyinsarsilmaz.lms.repository.BookRepository;

import com.huseyinsarsilmaz.lms.service.impl.BookServiceImpl;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book book1;
    private Book book2;

    private static Stream<Arguments> provideSearchParams() {
        return Stream.of(
                Arguments.of(Book.SearchType.TITLE, "code", (Function<Book, ?>) Book::getTitle),
                Arguments.of(Book.SearchType.AUTHOR, "Sarsılmaz", (Function<Book, ?>) Book::getAuthor),
                Arguments.of(Book.SearchType.ISBN, "123", (Function<Book, ?>) Book::getIsbn),
                Arguments.of(Book.SearchType.GENRE, "self-help", (Function<Book, ?>) Book::getGenre));
    }

    @BeforeEach
    public void setUp() {
        book1 = new Book();
        book1.setId(1L);
        book1.setTitle("How to write code");
        book1.setAuthor("Hüseyin Sarsılmaz");
        book1.setDescription("Best practices for writing Java code.");
        book1.setIsbn("978-0134685991");
        book1.setPublicationDate(LocalDate.of(2017, 6, 17));
        book1.setGenre(Book.Genre.BIOGRAPHY);
        book1.setIsAvailable(true);

        book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Jumping Jacks");
        book2.setAuthor("Jack the Jumper");
        book2.setDescription("Jumping jack exercises");
        book2.setIsbn("123-0134685992");
        book2.setPublicationDate(LocalDate.of(2021, 8, 17));
        book2.setGenre(Book.Genre.POETRY);
        book2.setIsAvailable(true);
    }

    @Test
    public void testCreate() {
        BookCreateRequest req = new BookCreateRequest();
        req.setTitle("How to write code");
        req.setAuthor("Hüseyin Sarsılmaz");
        req.setDescription("Best practices for writing Java code.");
        req.setIsbn("978-0134685991");
        req.setPublicationDate(LocalDate.of(2017, 6, 17));
        req.setGenre(Book.Genre.BIOGRAPHY);

        when(bookRepository.save(any(Book.class))).thenReturn(book1);

        Book createdBook = bookService.create(req);

        assertNotNull(createdBook);
        assertEquals(req.getTitle(), createdBook.getTitle());
        assertEquals(req.getAuthor(), createdBook.getAuthor());
        assertEquals(req.getIsbn(), createdBook.getIsbn());
    }

    @Test
    public void testIsIsbnTaken_whenIsbnExists() {
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(book1));

        assertThrows(AlreadyExistsException.class, () -> bookService.isIsbnTaken("978-0134685991"));
    }

    @Test
    public void testIsIsbnTaken_whenIsbnDoesNotExist() {
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> bookService.isIsbnTaken("978-0134686000"));
    }

    @Test
    public void testGetById_whenBookExists() {
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book1));

        Book result = bookService.getById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void testGetById_whenBookNotFound() {
        when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.getById(999L));
    }

    @Test
    public void testUpdate() {
        BookUpdateRequest req = new BookUpdateRequest();
        req.setTitle("Effective Java (3rd Edition)");
        req.setAuthor("Joshua Bloch");
        req.setDescription("Updated best practices for writing Java code.");
        req.setIsbn("978-0134685991");
        req.setPublicationDate(LocalDate.of(2018, 1, 1));
        req.setGenre(Book.Genre.BIOGRAPHY);

        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book updatedBook = bookService.update(book1, req);

        assertEquals(req.getTitle(), updatedBook.getTitle());
        assertEquals(req.getAuthor(), updatedBook.getAuthor());
        assertEquals(req.getIsbn(), updatedBook.getIsbn());
    }

    @Test
    public void testDelete() {
        doNothing().when(bookRepository).delete(any(Book.class));

        assertDoesNotThrow(() -> bookService.delete(book1));
        verify(bookRepository).delete(book1);
    }

    @ParameterizedTest
    @MethodSource("provideSearchParams")
    public void testSearchBooks(Book.SearchType type, String query, Function<Book, ?> fieldExtractor) {
        Book expectedBook = type == Book.SearchType.ISBN ? book2 : book1;
        Page<Book> expectedPage = new PageImpl<>(List.of(expectedBook));

        switch (type) {
            case TITLE ->
                when(bookRepository.findByTitleContainingIgnoreCase(eq(query), any(Pageable.class)))
                        .thenReturn(expectedPage);
            case AUTHOR ->
                when(bookRepository.findByAuthorContainingIgnoreCase(eq(query), any(Pageable.class)))
                        .thenReturn(expectedPage);
            case ISBN ->
                when(bookRepository.findByIsbnContainingIgnoreCase(eq(query), any(Pageable.class)))
                        .thenReturn(expectedPage);
            case GENRE ->
                when(bookRepository.findByGenre(eq(Book.Genre.POETRY), any(Pageable.class)))
                        .thenReturn(expectedPage);
        }

        Page<Book> result = bookService.searchBooks(type, query, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(fieldExtractor.apply(expectedBook), fieldExtractor.apply(result.getContent().get(0)));
    }

    @Test
    public void testSearchBooks_whenInvalidGenre() {
        assertThrows(NotFoundException.class,
                () -> bookService.searchBooks(Book.SearchType.GENRE, "WRONG-genre", Pageable.unpaged()));
    }

    @Test
    public void testChangeAvailability_whenAvailabilityChanges() {
        book1.setIsAvailable(false);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book updatedBook = bookService.updateAvailability(book1, true);

        assertTrue(updatedBook.getIsAvailable());
        verify(bookRepository).save(book1);
    }

    @Test
    public void testChangeAvailability_whenAvailabilityDoesNotChange() {

        Book updatedBook = bookService.updateAvailability(book1, true);

        assertTrue(updatedBook.getIsAvailable());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    public void testCheckAvailability_whenAvailable() {
        assertDoesNotThrow(() -> bookService.checkAvailability(book1));
    }

    @Test
    public void testCheckAvailability_whenNotAvailable() {
        book1.setIsAvailable(false);

        assertThrows(NotAvailableException.class, () -> bookService.checkAvailability(book1));
    }
}
