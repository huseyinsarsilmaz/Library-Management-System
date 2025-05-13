package com.huseyinsarsilmaz.lmsr.service.impl;

import org.springframework.stereotype.Service;

import com.huseyinsarsilmaz.lmsr.model.entity.Book;
import com.huseyinsarsilmaz.lmsr.repository.BookRepository;
import com.huseyinsarsilmaz.lmsr.service.BookService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    public Mono<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    public Mono<Void> updateAvailability(Mono<Book> bookMono, boolean available) {
        return bookMono.flatMap(book -> {
            book.setIsAvailable(available);
            return bookRepository.save(book).then();
        });
    }
}
