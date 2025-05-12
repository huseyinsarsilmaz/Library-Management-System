package com.huseyinsarsilmaz.lms.service.impl;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.huseyinsarsilmaz.lms.model.entity.Borrowing;
import com.huseyinsarsilmaz.lms.repository.ReactiveBorrowingRepository;
import com.huseyinsarsilmaz.lms.service.ReactiveBookService;
import com.huseyinsarsilmaz.lms.service.ReactiveBorrowingService;
import com.huseyinsarsilmaz.lms.service.ReactiveUserService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReactiveBorrowingServiceImpl implements ReactiveBorrowingService {

    private final ReactiveBorrowingRepository borrowingRepository;
    private final ReactiveUserService userService;
    private final ReactiveBookService bookService;

    private void updateBorrowingStatus(Borrowing borrowing, LocalDate now) {
        if (borrowing.getDueDate().isBefore(now)) {
            borrowing.setStatus(Borrowing.Status.RETURNED_OVERDUE);
        } else {
            borrowing.setStatus(Borrowing.Status.RETURNED_TIMELY);
        }
    }

    private Mono<Void> handleOverdueStatus(Borrowing borrowing) {
        if (borrowing.getStatus() != Borrowing.Status.RETURNED_OVERDUE) {
            return Mono.empty();
        }

        return borrowingRepository
                .countByBorrowerIdAndStatus(borrowing.getBorrower().getId(), Borrowing.Status.OVERDUE)
                .flatMap(overdueCount -> {
                    if (overdueCount >= 2) {
                        return userService.changeActive(borrowing.getBorrower(), false).then();
                    }
                    return Mono.empty();
                });
    }

    public Mono<Borrowing> returnBorrowing(Borrowing borrowing) {
        LocalDate now = LocalDate.now();
        borrowing.setReturnDate(now);

        updateBorrowingStatus(borrowing, now);

        return handleOverdueStatus(borrowing)
                .then(bookService.updateAvailability(borrowing.getBook(), true))
                .then(borrowingRepository.save(borrowing));
    }
}
