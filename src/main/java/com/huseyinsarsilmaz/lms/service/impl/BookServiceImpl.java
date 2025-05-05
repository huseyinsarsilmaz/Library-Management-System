package com.huseyinsarsilmaz.lms.service.impl;

import org.springframework.stereotype.Service;

import com.huseyinsarsilmaz.lms.exception.AlreadyExistsException;
import com.huseyinsarsilmaz.lms.model.dto.request.BookCreateRequest;
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

}
