package com.huseyinsarsilmaz.lms.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.huseyinsarsilmaz.lms.exception.AlreadyBorrowedException;
import com.huseyinsarsilmaz.lms.exception.AlreadyReturnedBorrowingException;
import com.huseyinsarsilmaz.lms.exception.BorrowingNotExcusableException;
import com.huseyinsarsilmaz.lms.exception.ForbiddenException;
import com.huseyinsarsilmaz.lms.exception.HasActiveBorrowingsException;
import com.huseyinsarsilmaz.lms.exception.NotFoundException;
import com.huseyinsarsilmaz.lms.exception.OverdueException;
import com.huseyinsarsilmaz.lms.model.dto.request.BorrowRequest;
import com.huseyinsarsilmaz.lms.model.entity.Book;
import com.huseyinsarsilmaz.lms.model.entity.Borrowing;
import com.huseyinsarsilmaz.lms.model.entity.User;
import com.huseyinsarsilmaz.lms.model.entity.Borrowing.Status;
import com.huseyinsarsilmaz.lms.repository.BorrowingRepository;
import com.huseyinsarsilmaz.lms.service.BookService;
import com.huseyinsarsilmaz.lms.service.BorrowingService;
import com.huseyinsarsilmaz.lms.service.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BorrowingServiceImpl implements BorrowingService {
    private final BorrowingRepository borrowingRepository;
    private final UserService userService;
    private final BookService bookService;

    private static final List<Borrowing.Status> ACTIVE_BORROWING_STATUSES = List
            .of(Borrowing.Status.BORROWED, Borrowing.Status.OVERDUE);

    private void updateBorrowingStatus(Borrowing borrowing, LocalDate now) {
        if (borrowing.getDueDate().isBefore(now)) {
            borrowing.setStatus(Borrowing.Status.RETURNED_OVERDUE);
        } else {
            borrowing.setStatus(Borrowing.Status.RETURNED_TIMELY);
        }
    }

    private void handleOverdueStatus(Borrowing borrowing) {
        if (borrowing.getStatus() == Borrowing.Status.RETURNED_OVERDUE) {
            long overdueCount = borrowingRepository.countByBorrowerIdAndStatus(borrowing.getBorrower().getId(),
                    Borrowing.Status.OVERDUE);
            if (overdueCount >= 2) {
                userService.changeActive(borrowing.getBorrower(), false);
            }
        }
    }

    public Borrowing create(BorrowRequest req) {
        boolean alreadyBorrowed = borrowingRepository.existsByBorrowerIdAndBookIdAndStatusIn(
                req.getBorrowerId(),
                req.getBookId(),
                ACTIVE_BORROWING_STATUSES);

        if (alreadyBorrowed) {
            throw new AlreadyBorrowedException();
        }

        User borrower = userService.getById(req.getBorrowerId());
        Book book = bookService.getById(req.getBookId());

        bookService.checkAvailability(book);
        book = bookService.updateAvailability(book, false);

        Borrowing newBorrowing = Borrowing.builder()
                .borrower(borrower)
                .book(book)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(7))
                .returnDate(null)
                .status(Borrowing.Status.BORROWED)
                .build();

        return borrowingRepository.save(newBorrowing);

    }

    public Borrowing getById(long id) {
        Optional<Borrowing> optBorrowing = borrowingRepository.findByIdWithBookAndBorrower(id);
        if (optBorrowing.isEmpty()) {
            throw new NotFoundException(Borrowing.class.getSimpleName(), "id");
        }

        return optBorrowing.get();
    }

    public void checkOwnership(User user, Borrowing borrowing) {
        if (user.getId() != borrowing.getBorrower().getId()) {
            throw new ForbiddenException();
        }
    }

    public void checkReturnable(Borrowing borrowing) {
        if (borrowing.isReturned()) {
            throw new AlreadyReturnedBorrowingException();
        }
    }

    @Transactional
    public Borrowing returnBorrowing(Borrowing borrowing) {
        LocalDate now = LocalDate.now();
        borrowing.setReturnDate(now);

        updateBorrowingStatus(borrowing, now);
        handleOverdueStatus(borrowing);

        bookService.updateAvailability(borrowing.getBook(), true);
        return borrowingRepository.save(borrowing);
    }


    public Page<Borrowing> getByBorrowerId(long borrowerId, Pageable pageable) {
        return borrowingRepository.findAllByBorrowerIdWithBook(borrowerId, pageable);

    }

    public void checkUserHasActiveBorrowings(User user) {
        if (borrowingRepository.countByBorrowerIdAndStatusIn(user.getId(), ACTIVE_BORROWING_STATUSES) > 0) {
            throw new HasActiveBorrowingsException();
        }
    }

    public Page<Borrowing> getOverdueByBorrowerId(long borrowerId, Pageable pageable) {
        return borrowingRepository.findAllByBorrowerIdAndStatusNotIn(borrowerId, ACTIVE_BORROWING_STATUSES, pageable);
    }

    public Page<Borrowing> getAllOverdue(Pageable pageable) {
        return borrowingRepository.findAllByStatusNotIn(ACTIVE_BORROWING_STATUSES, pageable);
    }

    public void checkBorrowableByBorrowerId(Long borrowerId) {
        userService.checkActiveById(borrowerId);

        if (borrowingRepository.existsByBorrowerIdAndStatus(borrowerId, Borrowing.Status.OVERDUE)) {
            throw new OverdueException();
        }
    }

    @Transactional
    public void excuseReturnedOverdueBorrowings(User user) {
        List<Borrowing> overdueBorrowings = borrowingRepository.findByBorrowerIdAndStatus(user.getId(),
                Status.RETURNED_OVERDUE);

        for (Borrowing borrowing : overdueBorrowings) {
            borrowing.setStatus(Borrowing.Status.RETURNED_EXCUSED);
        }

        borrowingRepository.saveAll(overdueBorrowings);
    }

    @Transactional
    public Borrowing excuseBorrowing(Borrowing borrowing) {
        borrowing.setStatus(Status.RETURNED_EXCUSED);

        return borrowingRepository.save(borrowing);
    }

    public void checkExcusable(Borrowing borrowing) {
        if (borrowing.getStatus() != Status.RETURNED_OVERDUE) {
            throw new BorrowingNotExcusableException();
        }
    }

    @Scheduled(cron = "0 0 19 * * *")
    @Transactional
    public void markOverdueBorrowings() {
        List<Borrowing> borrowings = borrowingRepository.findPastDueByStatus(Borrowing.Status.BORROWED,
                LocalDate.now());

        for (Borrowing borrowing : borrowings) {
            borrowing.setStatus(Borrowing.Status.OVERDUE);
        }

        borrowingRepository.saveAll(borrowings);
    }

}