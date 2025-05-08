package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

public class HasActiveBorrowingsException extends LmsException {

    public HasActiveBorrowingsException() {
        super("has.active.borrowings", new String[] {}, HttpStatus.FORBIDDEN);
    }

}
