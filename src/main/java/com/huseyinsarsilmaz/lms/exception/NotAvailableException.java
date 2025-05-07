package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

public class NotAvailableException extends LmsException {

    public NotAvailableException(String entity) {
        super("not.available", new String[] { entity }, HttpStatus.CONFLICT);
    }

}
