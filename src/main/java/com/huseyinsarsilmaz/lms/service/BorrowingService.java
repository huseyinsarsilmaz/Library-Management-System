package com.huseyinsarsilmaz.lms.service;

import com.huseyinsarsilmaz.lms.model.dto.request.BorrowRequest;
import com.huseyinsarsilmaz.lms.model.entity.Borrowing;
import com.huseyinsarsilmaz.lms.model.entity.User;

public interface BorrowingService {

    public Borrowing create(BorrowRequest req);

    public Borrowing getById(long id);

    public void checkOwnership(User user, Borrowing borrowing);

    public void checkReturnable(Borrowing borrowing);

    // Used entity name because return is a keyword
    public Borrowing returnBorrowing(Borrowing borrowing);

}
