package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

public final class NotAvailableException extends LmsException {
    public NotAvailableException(String entity) {
        super("not.available", HttpStatus.UNPROCESSABLE_ENTITY, entity);
    }
}