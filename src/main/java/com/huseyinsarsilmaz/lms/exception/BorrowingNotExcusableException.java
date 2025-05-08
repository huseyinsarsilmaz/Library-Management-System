package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

public class BorrowingNotExcusableException extends LmsException {

    public BorrowingNotExcusableException() {
        super("borrowing.not.excusable", new String[] {}, HttpStatus.CONFLICT);
    }

}
