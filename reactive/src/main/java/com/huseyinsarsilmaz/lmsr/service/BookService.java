package com.huseyinsarsilmaz.lmsr.service;

import org.springframework.stereotype.Service;

import com.huseyinsarsilmaz.lmsr.model.entity.Book;
import com.huseyinsarsilmaz.lmsr.repository.BookRepository;

import reactor.core.publisher.Mono;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Mono<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }
}
