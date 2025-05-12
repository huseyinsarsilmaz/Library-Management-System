package com.huseyinsarsilmaz.lms.service.impl;

import org.springframework.stereotype.Service;

import com.huseyinsarsilmaz.lms.model.entity.Book;
import com.huseyinsarsilmaz.lms.repository.ReactiveBookRepository;
import com.huseyinsarsilmaz.lms.service.ReactiveBookService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReactiveBookServiceImpl implements ReactiveBookService {

    private final ReactiveBookRepository bookRepository;

    @Override
    public Mono<Void> updateAvailability(Book book, boolean available) {
        book.setIsAvailable(available);
        return bookRepository.save(book).then();
    }
}
