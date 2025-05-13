package com.huseyinsarsilmaz.lmsr.service;

import java.util.List;


import com.huseyinsarsilmaz.lmsr.model.entity.Borrowing;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface BorrowingService {

    public Mono<Borrowing> returnBorrowing(Borrowing borrowing);

    public Flux<Borrowing> returnBorrowings(List<Long> borrowingIds);
}
