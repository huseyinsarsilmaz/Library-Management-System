package com.huseyinsarsilmaz.lmsr.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.huseyinsarsilmaz.lmsr.model.entity.Borrowing;

import reactor.core.publisher.Mono;

public interface BorrowingRepository extends ReactiveCrudRepository<Borrowing, Long> {
    Mono<Long> countByBorrowerIdAndStatus(Long borrowerId, Borrowing.Status status);

}