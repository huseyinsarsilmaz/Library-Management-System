package com.huseyinsarsilmaz.lms.service.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.huseyinsarsilmaz.lms.exception.AlreadyExistsException;
import com.huseyinsarsilmaz.lms.exception.NotFoundException;
import com.huseyinsarsilmaz.lms.model.dto.request.BookCreateRequest;
import com.huseyinsarsilmaz.lms.model.dto.request.BookUpdateRequest;
import com.huseyinsarsilmaz.lms.model.entity.Book;
import com.huseyinsarsilmaz.lms.repository.BookRepository;
import com.huseyinsarsilmaz.lms.service.BookService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    public Book create(BookCreateRequest req) {
        Book newBook = Book.builder()
                .title(req.getTitle())
                .author(req.getAuthor())
                .description(req.getDescription())
                .isbn(req.getIsbn())
                .publicationDate(req.getPublicationDate())
                .genre(req.getGenre())
                .build();

        return bookRepository.save(newBook);
    }

    public void isIsbnTaken(String isbn) {
        if (bookRepository.findByIsbn(isbn).isPresent()) {
            throw new AlreadyExistsException(Book.class.getSimpleName(), "isbn");
        }
    }

    public Book getById(long id) {
        Optional<Book> optBook = bookRepository.findById(id);
        if (optBook.isEmpty()) {
            throw new NotFoundException(Book.class.getSimpleName(), "id");
        }

        return optBook.get();
    }

    public Book update(Book book, BookUpdateRequest req) {

        book.setTitle(req.getTitle());
        book.setAuthor(req.getAuthor());
        book.setDescription(req.getDescription());
        book.setIsbn(req.getIsbn());
        book.setPublicationDate(req.getPublicationDate());
        book.setGenre(req.getGenre());

        return bookRepository.save(book);
    }

    public void delete(Book book) {
        bookRepository.delete(book);
    }

    public Page<Book> searchBooks(Book.SearchType searchType, String query, Pageable pageable) {
        return switch (searchType) {
            case TITLE -> bookRepository.findByTitleContainingIgnoreCase(query, pageable);
            case AUTHOR -> bookRepository.findByAuthorContainingIgnoreCase(query, pageable);
            case ISBN -> bookRepository.findByIsbnContainingIgnoreCase(query, pageable);
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

}
