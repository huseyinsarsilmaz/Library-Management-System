package com.huseyinsarsilmaz.lmsr.service;

import org.springframework.stereotype.Service;

import com.huseyinsarsilmaz.lmsr.model.entity.Borrowing;
import com.huseyinsarsilmaz.lmsr.repository.BorrowingRepository;

import reactor.core.publisher.Mono;

@Service
public class BorrowingService {

    private final BorrowingRepository borrowingRepository;

    public BorrowingService(BorrowingRepository borrowingRepository) {
        this.borrowingRepository = borrowingRepository;
    }

    public Mono<Borrowing> getBorrowingById(Long id) {
        return borrowingRepository.findById(id);
    }
}
