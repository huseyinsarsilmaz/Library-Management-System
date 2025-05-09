package com.huseyinsarsilmaz.lms.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import com.huseyinsarsilmaz.lms.model.entity.Book;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    private Book book1;

    private Book createBook(String title, String author, String isbn, Book.Genre genre) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setGenre(genre);
        return bookRepository.save(book);
    }

    @BeforeEach
    public void setUp() {
        book1 = createBook("How to write code", "Elon Musk", "1111111111111", Book.Genre.FICTION);
        createBook("How to fly into space", "Elon Musk", "1112223334445", Book.Genre.FICTION);
        createBook("Number 1", "Cristiano Ronaldo", "3333333333333", Book.Genre.BIOGRAPHY);
    }

    @Test
    public void testFindById() {
        Optional<Book> result = bookRepository.findById(book1.getId());
        assertTrue(result.isPresent());
        assertEquals(book1.getId(), result.get().getId());
    }

    @Test
    public void testFindById_whenNotFound() {
        Optional<Book> result = bookRepository.findById(Long.MAX_VALUE);
        assertFalse(result.isPresent());
    }

    @Test
    public void testFindByIsbn() {
        Optional<Book> result = bookRepository.findByIsbn("1111111111111");
        assertTrue(result.isPresent());
        assertEquals(book1.getId(), result.get().getId());
    }

    @Test
    public void testFindByIsbn_whenNotFound() {
        Optional<Book> result = bookRepository.findByIsbn("9999999999");
        assertFalse(result.isPresent());
    }

    @Test
    public void testFindByTitleContainingIgnoreCase() {
        Page<Book> result = bookRepository.findByTitleContainingIgnoreCase("how to", Pageable.ofSize(10));
        assertEquals(2, result.getTotalElements());
    }

    @Test
    public void testFindByTitleContainingIgnoreCase_whenNotFound() {
        Page<Book> result = bookRepository.findByTitleContainingIgnoreCase("the great gatsby", Pageable.ofSize(10));
        assertEquals(0, result.getTotalElements());
    }

    @Test
    public void testFindByAuthorContainingIgnoreCase() {
        Page<Book> result = bookRepository.findByAuthorContainingIgnoreCase("elon", Pageable.ofSize(10));
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(book -> book.getAuthor().equals("Elon Musk")));

    }

    @Test
    public void testFindByAuthorContainingIgnoreCase_whenNotFound() {
        Page<Book> result = bookRepository.findByAuthorContainingIgnoreCase("david", Pageable.ofSize(10));
        assertEquals(0, result.getTotalElements());
    }

    @Test
    public void testFindByIsbnContainingIgnoreCase() {
        Page<Book> result = bookRepository.findByIsbnContainingIgnoreCase("111", Pageable.ofSize(10));
        assertEquals(2, result.getTotalElements());
    }

    @Test
    public void testFindByIsbnContainingIgnoreCase_whenNotFound() {
        Page<Book> result = bookRepository.findByIsbnContainingIgnoreCase("999", Pageable.ofSize(10));
        assertEquals(0, result.getTotalElements());
    }

    @ParameterizedTest
    @CsvSource({
            // Format: Genre, expectedExists
            "FICTION, 2",
            "BIOGRAPHY, 1",
            "ROMANCE, 0"
    })
    public void testFindByGenre(Book.Genre genre, int expectedCount) {
        Page<Book> result = bookRepository.findByGenre(genre, Pageable.ofSize(10));
        assertEquals(expectedCount, result.getTotalElements());
    }
}
