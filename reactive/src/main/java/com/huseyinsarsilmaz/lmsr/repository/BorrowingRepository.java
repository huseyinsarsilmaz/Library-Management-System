package com.huseyinsarsilmaz.lmsr.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.huseyinsarsilmaz.lmsr.model.entity.Borrowing;

public interface BorrowingRepository extends ReactiveCrudRepository<Borrowing, Long> {

}