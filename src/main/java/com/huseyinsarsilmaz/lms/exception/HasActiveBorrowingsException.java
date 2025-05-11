package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

public final class HasActiveBorrowingsException extends LmsException {
    public HasActiveBorrowingsException() {
        super("has.active.borrowings", HttpStatus.FORBIDDEN);
    }
}