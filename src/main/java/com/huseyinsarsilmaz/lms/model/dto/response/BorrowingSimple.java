package com.huseyinsarsilmaz.lms.model.dto.response;

import java.time.LocalDate;

import com.huseyinsarsilmaz.lms.model.entity.Borrowing;

import lombok.Data;

@Data
public class BorrowingSimple {

    private final long id;
    private final UserSimple borrower;
    private final BookSimple book;
    private final LocalDate borrowDate;
    private final LocalDate dueDate;

    public BorrowingSimple(Borrowing borrowing) {
        this.id = borrowing.getId();
        this.borrower = new UserSimple(borrowing.getBorrower());
        this.book = new BookSimple(borrowing.getBook());
        this.borrowDate = borrowing.getBorrowDate();
        this.dueDate = borrowing.getDueDate();

    }
}