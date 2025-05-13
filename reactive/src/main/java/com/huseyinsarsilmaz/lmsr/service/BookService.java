package com.huseyinsarsilmaz.lmsr.service;

import com.huseyinsarsilmaz.lmsr.model.entity.Book;

import reactor.core.publisher.Mono;

public interface BookService {

    public Mono<Book> getBookById(Long id);

    public Mono<Void> updateAvailability(Mono<Book> bookMono, boolean available);

}
