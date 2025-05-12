package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

public final class AlreadyReturnedBorrowingException extends LmsException {
    public AlreadyReturnedBorrowingException() {
        super("already.returned.borrowing", HttpStatus.CONFLICT);
    }
}
