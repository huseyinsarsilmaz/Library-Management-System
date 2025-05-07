package com.huseyinsarsilmaz.lms.model.dto.response;

import java.time.LocalDate;

import com.huseyinsarsilmaz.lms.model.entity.Borrowing;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BorrowingDetailed extends BorrowingSimple {

    private final LocalDate returnDate;

    public BorrowingDetailed(Borrowing borrowing) {
        super(borrowing);
        this.returnDate = borrowing.getReturnDate();

    }
}