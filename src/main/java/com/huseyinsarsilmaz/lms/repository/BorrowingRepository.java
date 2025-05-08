package com.huseyinsarsilmaz.lms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.huseyinsarsilmaz.lms.model.entity.Borrowing;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {
    Optional<Borrowing> findByBorrowerIdAndBookId(Long borrowerId, Long bookId);

    boolean existsByBorrowerIdAndBookIdAndStatusIn(Long borrowerId, Long bookId, List<Borrowing.Status> statuses);

    boolean existsByBorrowerIdAndStatus(Long borrowerId, Borrowing.Status status);

    @Query("SELECT b FROM Borrowing b JOIN FETCH b.book JOIN FETCH b.borrower WHERE b.id = :id")
    Optional<Borrowing> findByIdWithBookAndBorrower(@Param("id") Long id);

    @Query("SELECT b FROM Borrowing b JOIN FETCH b.book WHERE b.borrower.id = :borrowerId")
    List<Borrowing> findAllByBorrowerIdWithBook(@Param("borrowerId") Long borrowerId);

    @Query("SELECT b FROM Borrowing b JOIN FETCH b.book WHERE b.borrower.id = :borrowerId AND b.status NOT IN (:statuses)")
    Page<Borrowing> findOverdueByBorrowerId(@Param("borrowerId") Long borrowerId,
            @Param("statuses") List<Borrowing.Status> statuses,
            Pageable pageable);

    @Query("SELECT b FROM Borrowing b JOIN FETCH b.book JOIN FETCH b.borrower WHERE b.status NOT IN (:statuses)")
    Page<Borrowing> findAllOverdue(@Param("statuses") List<Borrowing.Status> statuses,
            Pageable pageable);
}