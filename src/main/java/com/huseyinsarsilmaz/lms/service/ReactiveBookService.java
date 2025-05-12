package com.huseyinsarsilmaz.lms.service;

import com.huseyinsarsilmaz.lms.model.entity.Book;

import reactor.core.publisher.Mono;

public interface ReactiveBookService {
    Mono<Void> updateAvailability(Book book, boolean available);
}
