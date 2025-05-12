package com.huseyinsarsilmaz.lms.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.huseyinsarsilmaz.lms.exception.NotFoundException;
import com.huseyinsarsilmaz.lms.model.entity.Borrowing;
import com.huseyinsarsilmaz.lms.repository.BorrowingRepository;
import com.huseyinsarsilmaz.lms.service.ReactiveBookService;
import com.huseyinsarsilmaz.lms.service.ReactiveBorrowingService;
import com.huseyinsarsilmaz.lms.service.ReactiveUserService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class ReactiveBorrowingServiceImpl implements ReactiveBorrowingService {

    private final BorrowingRepository borrowingRepository;
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

        return Mono.fromCallable(() -> borrowingRepository.countByBorrowerIdAndStatus(
                borrowing.getBorrower().getId(),
                Borrowing.Status.OVERDUE))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(overdueCount -> {
                    if (overdueCount >= 2) {
                        return userService.changeActive(borrowing.getBorrower(), false).then();
                    }
                    return Mono.empty();
                });
    }

    public Mono<Borrowing> returnBorrowing(Borrowing borrowing) {
        return Mono.fromCallable(() -> {
            LocalDate now = LocalDate.now();
            borrowing.setReturnDate(now);
            updateBorrowingStatus(borrowing, now);
            return borrowing;
        })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(b -> handleOverdueStatus(b)
                        .then(bookService.updateAvailability(b.getBook(), true))
                        .then(Mono.fromCallable(() -> borrowingRepository.save(b)))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    @Override
    public Flux<Borrowing> returnBorrowings(List<Long> borrowingIds) {
        return Flux.fromIterable(borrowingIds)
                .flatMap(id -> Mono.fromCallable(() -> borrowingRepository.findById(id))
                        .subscribeOn(Schedulers.boundedElastic())
                        .flatMap(optional -> optional
                                .map(Mono::just)
                                .orElseGet(() -> Mono.error(new NotFoundException("Borrowing", "id")))))
                .flatMap(this::returnBorrowing);
    }
}
