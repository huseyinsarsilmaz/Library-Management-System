package com.huseyinsarsilmaz.lms.service.impl;

import org.springframework.stereotype.Service;

import com.huseyinsarsilmaz.lms.model.entity.Book;
import com.huseyinsarsilmaz.lms.repository.BookRepository;
import com.huseyinsarsilmaz.lms.service.ReactiveBookService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReactiveBookServiceImpl implements ReactiveBookService {

    private final BookRepository bookRepository;

    public Mono<Void> updateAvailability(Book book, boolean available) {
        return Mono.fromRunnable(() -> {
            book.setIsAvailable(available);
            bookRepository.save(book);
        });
    }
}