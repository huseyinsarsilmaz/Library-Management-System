package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

public final class AlreadyBorrowedException extends LmsException {
    public AlreadyBorrowedException() {
        super("already.borrowed", HttpStatus.CONFLICT);
    }
}
