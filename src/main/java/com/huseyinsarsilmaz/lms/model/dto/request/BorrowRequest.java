package com.huseyinsarsilmaz.lms.model.dto.request;

import java.time.LocalDate;

import com.huseyinsarsilmaz.lms.validation.Alphabethical;
import com.huseyinsarsilmaz.lms.validation.PhoneNumber;
import com.huseyinsarsilmaz.lms.validation.RequiredField;
import com.huseyinsarsilmaz.lms.validation.StrSize;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BorrowRequest {

    @RequiredField(entityName = "Borrower", fieldName = "id")
    private Long borrowerId;

    @RequiredField(entityName = "Book", fieldName = "id")
    private Long bookId;

    @RequiredField(entityName = "Borrow", fieldName = "date")
    @FutureOrPresent(message = "{fail.date.future.present}")
    private LocalDate borrowDate;

    @RequiredField(entityName = "Due", fieldName = "date")
    @Future(message = "{fail.date.future}")
    private LocalDate dueDate;

    @RequiredField(entityName = "Due", fieldName = "date")
    @Future(message = "{fail.date.future}")
    private LocalDate returnDate;

}
