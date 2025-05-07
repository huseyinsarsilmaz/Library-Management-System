package com.huseyinsarsilmaz.lms.model.dto.response;

import java.time.LocalDate;

import com.huseyinsarsilmaz.lms.model.entity.Book;
import com.huseyinsarsilmaz.lms.model.entity.Borrowing;
import com.huseyinsarsilmaz.lms.model.entity.User;

import lombok.Data;

@Data
public class BorrowingSimple {

    private final long id;
    private final User borrower;
    private final Book book;
    private final LocalDate dueDate;

    public BorrowingSimple(Borrowing borrowing) {
        this.id = borrowing.getId();
        this.borrower = borrowing.getBorrower();
        this.book = borrowing.getBook();
        this.dueDate = borrowing.getDueDate();

    }
}