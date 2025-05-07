package com.huseyinsarsilmaz.lms.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.huseyinsarsilmaz.lms.exception.AlreadyBorrowedException;
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

        Borrowing newBorrowing = Borrowing.builder()
                .borrower(borrower)
                .book(book)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(7))
                .returnDate(null)
                .build();

        return borrowingRepository.save(newBorrowing);

    }

}
