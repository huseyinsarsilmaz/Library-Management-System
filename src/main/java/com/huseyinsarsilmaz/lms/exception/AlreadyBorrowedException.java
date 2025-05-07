package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

public class AlreadyBorrowedException extends LmsException {

    public AlreadyBorrowedException() {
        super("already.borrowed", new String[] {}, HttpStatus.CONFLICT);
    }

}
