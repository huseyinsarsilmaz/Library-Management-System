package com.huseyinsarsilmaz.lms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.huseyinsarsilmaz.lms.model.entity.Borrowing;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {
    Optional<Borrowing> findByBorrowerIdAndBookId(Long borrowerId, Long bookId);

}