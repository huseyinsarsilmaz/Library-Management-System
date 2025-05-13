package com.huseyinsarsilmaz.lms.model.dto.request;

import com.huseyinsarsilmaz.lms.validation.PositiveNumber;
import com.huseyinsarsilmaz.lms.validation.RequiredField;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRequest {

    @PositiveNumber(entityName = "borrower", fieldName = "id")
    private Long borrowerId;

    @RequiredField(entityName = "Book", fieldName = "id")
    @PositiveNumber(entityName = "book", fieldName = "id")
    private Long bookId;

}
