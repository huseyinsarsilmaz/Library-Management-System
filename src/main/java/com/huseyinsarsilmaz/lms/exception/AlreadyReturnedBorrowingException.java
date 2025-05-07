package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

public class AlreadyReturnedBorrowingException extends LmsException {

    public AlreadyReturnedBorrowingException() {
        super("already.returned.borrowing", new String[] {}, HttpStatus.CONFLICT);
    }

}
