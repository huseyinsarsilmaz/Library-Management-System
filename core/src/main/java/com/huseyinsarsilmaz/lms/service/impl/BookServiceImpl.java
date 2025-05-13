package com.huseyinsarsilmaz.lms.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.huseyinsarsilmaz.lms.exception.AlreadyExistsException;
import com.huseyinsarsilmaz.lms.exception.NotAvailableException;
import com.huseyinsarsilmaz.lms.exception.NotFoundException;
import com.huseyinsarsilmaz.lms.model.dto.request.BookCreateRequest;
import com.huseyinsarsilmaz.lms.model.dto.request.BookUpdateRequest;
import com.huseyinsarsilmaz.lms.model.entity.Book;
import com.huseyinsarsilmaz.lms.model.mapper.BookMapper;
import com.huseyinsarsilmaz.lms.repository.BookRepository;
import com.huseyinsarsilmaz.lms.service.BookService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    private String convertToCompactIsbn(String rawIsbn) {
        return rawIsbn.trim().toUpperCase().replaceAll("[^0-9X]", "");
    }

    public Book create(BookCreateRequest req) {
        isIsbnTaken(req.getIsbn());
        req.setIsbn(convertToCompactIsbn(req.getIsbn()));
        return bookRepository.save(bookMapper.toEntity(req));
    }

    public void isIsbnTaken(String isbn) {
        if (bookRepository.findByIsbn(isbn).isPresent()) {
            throw new AlreadyExistsException("Book", "isbn");
        }
    }

    public Book getById(long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book", "id"));
    }

    @Transactional
    public Book update(Book book, BookUpdateRequest req) {
        req.setIsbn(convertToCompactIsbn(req.getIsbn()));
        bookMapper.updateEntity(book, req);
        return bookRepository.save(book);
    }

    @Transactional
    public void delete(Book book) {
        bookRepository.delete(book);
    }

    public Page<Book> searchBooks(Book.SearchType searchType, String query, Pageable pageable) {

        return switch (searchType) {
            case TITLE -> bookRepository.findByTitleContainingIgnoreCase(query, pageable);
            case AUTHOR -> bookRepository.findByAuthorContainingIgnoreCase(query, pageable);
            case ISBN -> bookRepository.findByIsbnContainingIgnoreCase(convertToCompactIsbn(query), pageable);
            case GENRE -> {
                Book.Genre genre;
                try {
                    genre = Book.Genre.valueOf(query.toUpperCase().replace('-', '_'));
                } catch (IllegalArgumentException e) {
                    throw new NotFoundException("Genre", "name");
                }
                yield bookRepository.findByGenre(genre, pageable);
            }
        };
    }

    @Transactional
    public Book updateAvailability(Book book, boolean newAvailability) {
        if (!book.getIsAvailable().equals(newAvailability)) {
            book.setIsAvailable(newAvailability);
            return bookRepository.save(book);
        }
        return book;
    }

    public void checkAvailability(Book book) {
        if (!book.getIsAvailable()) {
            throw new NotAvailableException("Book");
        }
    }
}
