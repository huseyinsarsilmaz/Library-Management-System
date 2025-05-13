package com.huseyinsarsilmaz.lmsr.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.huseyinsarsilmaz.lmsr.model.entity.Borrowing;
import com.huseyinsarsilmaz.lmsr.repository.BorrowingRepository;
import com.huseyinsarsilmaz.lmsr.service.BookService;
import com.huseyinsarsilmaz.lmsr.service.BorrowingService;
import com.huseyinsarsilmaz.lmsr.service.UserService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BorrowingServiceImpl implements BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final BookService bookService;
    private final UserService userService;

    private void updateBorrowingStatus(Borrowing borrowing, LocalDate now) {
        if (borrowing.getDueDate().isBefore(now)) {
            borrowing.setStatus(Borrowing.Status.RETURNED_OVERDUE);
        } else {
            borrowing.setStatus(Borrowing.Status.RETURNED_TIMELY);
        }
    }

    private Mono<Void> handleOverdueStatus(Borrowing borrowing) {
        if (borrowing.getStatus() == Borrowing.Status.RETURNED_OVERDUE) {
            return borrowingRepository.countByBorrowerIdAndStatus(
                    borrowing.getBorrowerId(),
                    Borrowing.Status.OVERDUE)
                    .flatMap(overdueCount -> {
                        if (overdueCount >= 2) {
                            return userService.changeActive(userService.getUserById(borrowing.getBorrowerId()), false)
                                    .then();
                        }
                        return Mono.empty();
                    });
        }
        return Mono.empty();
    }

    public Mono<Borrowing> returnBorrowing(Borrowing borrowing) {
        return borrowingRepository.findById(borrowing.getId())
                .switchIfEmpty(
                        Mono.error(new RuntimeException("Borrowing not found with id " + borrowing.getId())))
                .flatMap(existingBorrowing -> {
                    LocalDate now = LocalDate.now();
                    existingBorrowing.setReturnDate(now);

                    updateBorrowingStatus(existingBorrowing, now);

                    return handleOverdueStatus(existingBorrowing)
                            .then(bookService.updateAvailability(
                                    bookService.getBookById(existingBorrowing.getBookId()),
                                    true))
                            .then(borrowingRepository.save(existingBorrowing));
                });
    }

    public Flux<Borrowing> returnBorrowings(List<Long> borrowingIds) {
        return borrowingRepository.findAllById(borrowingIds)
                .flatMap(borrowing -> returnBorrowing(borrowing))
                .doOnError(e -> System.out.println("Error occurred: " + e.getMessage())); // Log any errors
    }
}
