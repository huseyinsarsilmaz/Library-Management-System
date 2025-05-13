package com.huseyinsarsilmaz.lms.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.huseyinsarsilmaz.lms.model.dto.request.BorrowRequest;
import com.huseyinsarsilmaz.lms.model.dto.response.BorrowingDetailed;
import com.huseyinsarsilmaz.lms.model.entity.Borrowing;
import com.huseyinsarsilmaz.lms.model.entity.User;

public interface BorrowingService {

    public Borrowing create(BorrowRequest req);

    public Borrowing getById(long id);

    public void checkOwnership(User user, Borrowing borrowing);

    public void checkReturnable(Borrowing borrowing);

    // Used entity name because return is a keyword
    public Borrowing returnBorrowing(Borrowing borrowing);

    public Page<Borrowing> getByBorrowerId(long borrowerId, Pageable pageable);

    public Page<Borrowing> getOverdueByBorrowerId(long borrowerId, Pageable pageable);

    public Page<Borrowing> getAllOverdue(Pageable pageable);

    public void checkBorrowableByBorrowerId(Long borrowerId);

    public void checkUserHasActiveBorrowings(User user);

    public void excuseReturnedOverdueBorrowings(User user);

    public Borrowing excuseBorrowing(Borrowing borrowing);

    public void checkExcusable(Borrowing borrowing);

    public void printOverdueReport(Page<BorrowingDetailed> page);

}
