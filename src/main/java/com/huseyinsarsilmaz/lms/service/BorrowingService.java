package com.huseyinsarsilmaz.lms.service;

import com.huseyinsarsilmaz.lms.model.dto.request.BorrowRequest;
import com.huseyinsarsilmaz.lms.model.entity.Borrowing;

public interface BorrowingService {

    public Borrowing create(BorrowRequest req);
}
