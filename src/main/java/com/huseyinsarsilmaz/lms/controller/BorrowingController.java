package com.huseyinsarsilmaz.lms.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.huseyinsarsilmaz.lms.model.dto.request.BorrowRequest;
import com.huseyinsarsilmaz.lms.model.dto.response.ApiResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.BorrowingDetailed;
import com.huseyinsarsilmaz.lms.model.dto.response.BorrowingSimple;
import com.huseyinsarsilmaz.lms.model.dto.response.PagedResponse;
import com.huseyinsarsilmaz.lms.model.entity.Borrowing;
import com.huseyinsarsilmaz.lms.model.entity.User;
import com.huseyinsarsilmaz.lms.security.CurrentUser;
import com.huseyinsarsilmaz.lms.service.BorrowingService;
import com.huseyinsarsilmaz.lms.service.UserService;
import com.huseyinsarsilmaz.lms.util.ResponseBuilder;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/borrowings")
@RequiredArgsConstructor
public class BorrowingController {
    private final BorrowingService borrowingService;
    private final UserService userService;
    private final ResponseBuilder responseBuilder;

    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    @PostMapping
    public ResponseEntity<ApiResponse<BorrowingSimple>> createBorrowing(
            @CurrentUser User myUser,
            @Valid @RequestBody BorrowRequest req) {

        if (req.getBorrowerId() != null) {
            userService.checkRole(myUser, User.Role.ROLE_LIBRARIAN);
        } else {
            req.setBorrowerId(myUser.getId());
        }

        borrowingService.checkBorrowableByBorrowerId(req.getBorrowerId());

        Borrowing newBorrowing = borrowingService.create(req);

        return responseBuilder.success("Borrowing", "created", new BorrowingSimple(newBorrowing), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<ApiResponse<BorrowingDetailed>> returnBorrowing(
            @CurrentUser User myUser,
            @PathVariable("id") long id) {

        Borrowing borrowing = borrowingService.getById(id);

        borrowingService.checkOwnership(myUser, borrowing);
        borrowingService.checkReturnable(borrowing);

        borrowing = borrowingService.returnBorrowing(borrowing);

        return responseBuilder.success("Borrowing", "returned", new BorrowingDetailed(borrowing), HttpStatus.OK);
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PagedResponse<BorrowingSimple>>> getMyBorrowingHistory(
            @CurrentUser User myUser,
            @PageableDefault(size = 10, sort = "borrower") Pageable pageable) {

        Page<Borrowing> borrowings = borrowingService.getByBorrowerId(myUser.getId(), pageable);
        Page<BorrowingSimple> page = borrowings.map(BorrowingDetailed::new);


        return responseBuilder.success("Borrowing history", "fetched", new PagedResponse<>(page), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    @GetMapping("/user/{id}")
    public ResponseEntity<ApiResponse<PagedResponse<BorrowingSimple>>> getBorrowingHistory(
            @PathVariable("id") long id,
            @PageableDefault(size = 10, sort = "borrower") Pageable pageable) {

        User borrowedUser = userService.getById(id);
        Page<Borrowing> borrowings = borrowingService.getByBorrowerId(borrowedUser.getId(), pageable);
        Page<BorrowingSimple> page = borrowings.map(BorrowingDetailed::new);

        return responseBuilder.success("Borrowing history", "fetched", new PagedResponse<>(page), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    @GetMapping("/report")
    public ResponseEntity<ApiResponse<PagedResponse<BorrowingSimple>>> getBorrowingReport(
            @RequestParam(required = false) Long borrowerId,
            @PageableDefault(size = 10, sort = "borrower") Pageable pageable) {

        Page<Borrowing> borrowings = (borrowerId == null)
                ? borrowingService.getAllOverdue(pageable)
                : borrowingService.getOverdueByBorrowerId(borrowerId, pageable);

        Page<BorrowingSimple> page = borrowings.map(BorrowingDetailed::new);

        return responseBuilder.success("Borrowing overdue report", "acquired", new PagedResponse<>(page),
                HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    @PostMapping("/{id}/excuse")
    public ResponseEntity<ApiResponse<BorrowingDetailed>> excuseBorrowing(@PathVariable("id") long id) {

        Borrowing borrowing = borrowingService.getById(id);
        borrowingService.checkExcusable(borrowing);

        borrowing = borrowingService.excuseBorrowing(borrowing);

        return responseBuilder.success("Borrowing", "excused", new BorrowingDetailed(borrowing), HttpStatus.OK);
    }


}
