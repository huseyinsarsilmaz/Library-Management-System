package com.huseyinsarsilmaz.lms.model.dto.response;

import java.time.LocalDate;

import com.huseyinsarsilmaz.lms.model.entity.Borrowing;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BorrowingSimple {

    private long id;
    private UserSimple borrower;
    private BookSimple book;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private Borrowing.Status status;

}