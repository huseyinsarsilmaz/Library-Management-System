package com.huseyinsarsilmaz.lms.service;

import java.util.List;

import com.huseyinsarsilmaz.lms.model.entity.Borrowing;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveBorrowingService {
    public Mono<Borrowing> returnBorrowing(Borrowing borrowing);

    public Flux<Borrowing> returnBorrowings(List<Long> borrowingIds);
}
