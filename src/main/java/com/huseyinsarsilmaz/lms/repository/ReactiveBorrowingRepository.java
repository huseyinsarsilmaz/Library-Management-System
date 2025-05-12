package com.huseyinsarsilmaz.lms.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.huseyinsarsilmaz.lms.model.entity.Borrowing;

import reactor.core.publisher.Mono;

@Repository
public interface ReactiveBorrowingRepository extends ReactiveCrudRepository<Borrowing, Long> {

    Mono<Long> countByBorrowerIdAndStatus(Long borrowerId, Borrowing.Status status);
}
