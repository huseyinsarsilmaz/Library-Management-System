package com.huseyinsarsilmaz.lms.repository;

import java.time.LocalDate;
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

    long countByBorrowerIdAndStatus(Long borrowerId, Borrowing.Status status);

    long countByBorrowerIdAndStatusIn(Long borrowerId, List<Borrowing.Status> statuses);

    List<Borrowing> findByBorrowerIdAndStatus(Long borrowerId, Borrowing.Status status);

    @Query("SELECT b FROM Borrowing b WHERE b.status = :status AND b.dueDate < :today")
    List<Borrowing> findPastDueByStatus(@Param("status") Borrowing.Status status, @Param("today") LocalDate today);

    @Query("SELECT b FROM Borrowing b JOIN FETCH b.book JOIN FETCH b.borrower WHERE b.id = :id")
    Optional<Borrowing> findByIdWithBookAndBorrower(@Param("id") Long id);

    @Query("SELECT b FROM Borrowing b JOIN FETCH b.book WHERE b.borrower.id = :borrowerId")
    List<Borrowing> findAllByBorrowerIdWithBook(@Param("borrowerId") Long borrowerId);

    @Query("SELECT b FROM Borrowing b JOIN FETCH b.book WHERE b.borrower.id = :borrowerId AND b.status NOT IN (:statuses)")
    Page<Borrowing> findAllByBorrowerIdAndStatusNotIn(@Param("borrowerId") Long borrowerId,
            @Param("statuses") List<Borrowing.Status> statuses,
            Pageable pageable);

    @Query("SELECT b FROM Borrowing b JOIN FETCH b.book JOIN FETCH b.borrower WHERE b.status NOT IN (:statuses)")
    Page<Borrowing> findAllByStatusNotIn(@Param("statuses") List<Borrowing.Status> statuses,
            Pageable pageable);
}