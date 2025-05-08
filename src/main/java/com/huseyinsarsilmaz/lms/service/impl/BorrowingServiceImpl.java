package com.huseyinsarsilmaz.lms.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.huseyinsarsilmaz.lms.exception.AlreadyBorrowedException;
import com.huseyinsarsilmaz.lms.exception.AlreadyReturnedBorrowingException;
import com.huseyinsarsilmaz.lms.exception.ForbiddenException;
import com.huseyinsarsilmaz.lms.exception.NotFoundException;
import com.huseyinsarsilmaz.lms.exception.OverdueException;
import com.huseyinsarsilmaz.lms.model.dto.request.BorrowRequest;
import com.huseyinsarsilmaz.lms.model.entity.Book;
import com.huseyinsarsilmaz.lms.model.entity.Borrowing;
import com.huseyinsarsilmaz.lms.model.entity.User;
import com.huseyinsarsilmaz.lms.repository.BorrowingRepository;
import com.huseyinsarsilmaz.lms.service.BookService;
import com.huseyinsarsilmaz.lms.service.BorrowingService;
import com.huseyinsarsilmaz.lms.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BorrowingServiceImpl implements BorrowingService {
    private final BorrowingRepository borrowingRepository;
    private final UserService userService;
    private final BookService bookService;

    private static final List<Borrowing.Status> ACTIVE_BORROWING_STATUSES = List
            .of(Borrowing.Status.BORROWED, Borrowing.Status.OVERDUE);

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
        book = bookService.changeAvailability(book, false);

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

    public Borrowing returnBorrowing(Borrowing borrowing) {

        LocalDate now = LocalDate.now();
        borrowing.setReturnDate(now);

        if (borrowing.getDueDate().isBefore(now)) {
            borrowing.setStatus(Borrowing.Status.RETURNED_OVERDUE);
        } else {
            borrowing.setStatus(Borrowing.Status.RETURNED_TIMELY);
        }

        if (borrowing.getStatus() == Borrowing.Status.RETURNED_OVERDUE) {
            long overdueCount = borrowingRepository.countByBorrowerIdAndStatus(borrowing.getBorrower().getId(),
                    Borrowing.Status.OVERDUE);
            if (overdueCount >= 2) {
                userService.changeActive(borrowing.getBorrower(), false);
            }
        }

        bookService.changeAvailability(borrowing.getBook(), true);
        return borrowingRepository.save(borrowing);
    }

    public List<Borrowing> getByBorrowerId(long borrowerId) {
        return borrowingRepository.findAllByBorrowerIdWithBook(borrowerId);

    }

    public long getActiveBorrowingCountByBorrowerId(long borrowerId) {
        return borrowingRepository.countByBorrowerIdAndStatusIn(borrowerId, ACTIVE_BORROWING_STATUSES);

    }

    public Page<Borrowing> getOverdueByBorrowerId(long borrowerId, Pageable pageable) {
        return borrowingRepository.findOverdueByBorrowerId(borrowerId, ACTIVE_BORROWING_STATUSES, pageable);
    }

    public Page<Borrowing> getAllOverdue(Pageable pageable) {
        return borrowingRepository.findAllOverdue(ACTIVE_BORROWING_STATUSES, pageable);
    }

    public void checkBorrowableByBorrowerId(Long borrowerId) {
        userService.checkActiveById(borrowerId);

        if (borrowingRepository.existsByBorrowerIdAndStatus(borrowerId, Borrowing.Status.OVERDUE)) {
            throw new OverdueException();
        }
    }

}
