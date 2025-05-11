package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

public final class BorrowingNotExcusableException extends LmsException {
    public BorrowingNotExcusableException() {
        super("borrowing.not.excusable", HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
