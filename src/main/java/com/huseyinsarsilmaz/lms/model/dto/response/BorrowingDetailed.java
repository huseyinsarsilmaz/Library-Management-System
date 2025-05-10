package com.huseyinsarsilmaz.lms.model.dto.response;

import java.time.LocalDate;

import com.huseyinsarsilmaz.lms.model.entity.Borrowing;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class BorrowingDetailed extends BorrowingSimple {

    private LocalDate returnDate;

    public BorrowingDetailed(Borrowing borrowing) {
        super(borrowing);
        this.returnDate = borrowing.getReturnDate();

    }
}