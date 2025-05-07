package com.huseyinsarsilmaz.lms.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huseyinsarsilmaz.lms.model.dto.request.BorrowRequest;
import com.huseyinsarsilmaz.lms.model.dto.response.ApiResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.BorrowingDetailed;
import com.huseyinsarsilmaz.lms.model.dto.response.BorrowingHistory;
import com.huseyinsarsilmaz.lms.model.dto.response.BorrowingSimple;
import com.huseyinsarsilmaz.lms.model.entity.Borrowing;
import com.huseyinsarsilmaz.lms.model.entity.User;
import com.huseyinsarsilmaz.lms.service.BorrowingService;
import com.huseyinsarsilmaz.lms.service.UserService;
import com.huseyinsarsilmaz.lms.service.Utils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/borrowings")
@RequiredArgsConstructor
public class BorrowingController {
    private final BorrowingService borrowingService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse> createBorrowing(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody BorrowRequest req) {

        User myUser = userService.getFromToken(token);

        if (req.getBorrowerId() != null) {
            userService.checkRole(myUser, User.Role.ROLE_LIBRARIAN);
        } else {
            req.setBorrowerId(myUser.getId());
        }

        Borrowing newBorring = borrowingService.create(req);

        return Utils.successResponse(Borrowing.class.getSimpleName(), "created", new BorrowingSimple(newBorring),
                HttpStatus.CREATED);
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<ApiResponse> returnBorrowing(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") long id) {

        User myUser = userService.getFromToken(token);
        Borrowing borrowing = borrowingService.getById(id);

        borrowingService.checkOwnership(myUser, borrowing);
        borrowingService.checkReturnable(borrowing);

        borrowing = borrowingService.returnBorrowing(borrowing);

        return Utils.successResponse(Borrowing.class.getSimpleName(), "returned", new BorrowingDetailed(borrowing),
                HttpStatus.OK);
    }

    private BorrowingHistory getBorrowingHistory(List<Borrowing> borrowings) {
        List<BorrowingSimple> active = new ArrayList<>();
        List<BorrowingDetailed> returned = new ArrayList<>();

        for (Borrowing borrowing : borrowings) {
            if (borrowing.isReturned()) {
                returned.add(new BorrowingDetailed(borrowing));
            } else {
                active.add(new BorrowingSimple(borrowing));
            }
        }

        return new BorrowingHistory(active, returned);
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse> getMyBorrowingHistory(@RequestHeader("Authorization") String token) {

        User myUser = userService.getFromToken(token);
        List<Borrowing> borrowings = borrowingService.getByBorrowerId(myUser.getId());
        BorrowingHistory borrowingHistory = getBorrowingHistory(borrowings);

        return Utils.successResponse(Borrowing.class.getSimpleName() + " history", "acquired", borrowingHistory,
                HttpStatus.OK);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<ApiResponse> getBorrowingHistory(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") long id) {

        User myUser = userService.getFromToken(token);
        userService.checkRole(myUser, User.Role.ROLE_LIBRARIAN);

        List<Borrowing> borrowings = borrowingService.getByBorrowerId(id);
        BorrowingHistory borrowingHistory = getBorrowingHistory(borrowings);

        return Utils.successResponse(Borrowing.class.getSimpleName() + " history", "acquired", borrowingHistory,
                HttpStatus.OK);
    }

}
