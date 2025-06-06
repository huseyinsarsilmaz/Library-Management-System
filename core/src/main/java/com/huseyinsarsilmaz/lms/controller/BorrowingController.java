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
import com.huseyinsarsilmaz.lms.model.dto.response.LmsApiResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.BorrowingDetailed;
import com.huseyinsarsilmaz.lms.model.dto.response.BorrowingSimple;
import com.huseyinsarsilmaz.lms.model.dto.response.PagedResponse;
import com.huseyinsarsilmaz.lms.model.entity.Borrowing;
import com.huseyinsarsilmaz.lms.model.entity.User;
import com.huseyinsarsilmaz.lms.model.mapper.BorrowingMapper;
import com.huseyinsarsilmaz.lms.security.CurrentUser;
import com.huseyinsarsilmaz.lms.service.BorrowingService;
import com.huseyinsarsilmaz.lms.service.UserService;
import com.huseyinsarsilmaz.lms.util.LmsResponseBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/borrowings")
@RequiredArgsConstructor
public class BorrowingController {
    private final BorrowingService borrowingService;
    private final UserService userService;
    private final LmsResponseBuilder responseBuilder;
    private final BorrowingMapper borrowingMapper;

    @PostMapping
    @Operation(summary = "Create a new borrowing", description = "Creates a new borrowing record for a book.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Borrowing successfully created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BorrowingSimple.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid borrowing request data", content = @Content)
    })
    public ResponseEntity<LmsApiResponse<BorrowingSimple>> createBorrowing(
            @CurrentUser User myUser,
            @Valid @RequestBody BorrowRequest req) {

        if (req.getBorrowerId() != null) {
            userService.checkHasRole(myUser, User.Role.ROLE_LIBRARIAN);
        } else {
            req.setBorrowerId(myUser.getId());
        }

        borrowingService.checkBorrowableByBorrowerId(req.getBorrowerId());

        Borrowing newBorrowing = borrowingService.create(req);

        return responseBuilder.success("Borrowing", "created", borrowingMapper.toDtoSimple(newBorrowing),
                HttpStatus.CREATED);
    }

    @PostMapping("/{id}/return")
    @Operation(summary = "Return a borrowed book", description = "Marks a book as returned and updates the borrowing record.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Borrowing successfully returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BorrowingDetailed.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Borrowing cannot be returned", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not Found - Borrowing record not found", content = @Content)
    })
    public ResponseEntity<LmsApiResponse<BorrowingDetailed>> returnBorrowing(
            @CurrentUser User myUser,
            @PathVariable("id") long id) {

        Borrowing borrowing = borrowingService.getById(id);

        borrowingService.checkOwnership(myUser, borrowing);
        borrowingService.checkReturnable(borrowing);

        borrowing = borrowingService.returnBorrowing(borrowing);

        return responseBuilder.success("Borrowing", "returned", borrowingMapper.toDtoDetailed(borrowing),
                HttpStatus.OK);
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's borrowing history", description = "Fetches the borrowing history of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Borrowing history fetched successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class)))
    })
    public ResponseEntity<LmsApiResponse<PagedResponse<BorrowingSimple>>> getMyBorrowingHistory(
            @CurrentUser User myUser,
            @PageableDefault(size = 10, sort = "borrower") Pageable pageable) {

        Page<Borrowing> borrowings = borrowingService.getByBorrowerId(myUser.getId(), pageable);
        Page<BorrowingSimple> page = borrowings.map(borrowingMapper::toDtoDetailed);

        return responseBuilder.success("Borrowing history", "fetched", new PagedResponse<>(page), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    @GetMapping("/user/{id}")
    @Operation(summary = "Get a user's borrowing history", description = "Fetches the borrowing history of a specific user by their ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Borrowing history fetched successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found - User not found", content = @Content)
    })
    public ResponseEntity<LmsApiResponse<PagedResponse<BorrowingDetailed>>> getBorrowingHistory(
            @PathVariable("id") long id,
            @PageableDefault(size = 10, sort = "borrower") Pageable pageable) {

        User borrowedUser = userService.getById(id);
        Page<Borrowing> borrowings = borrowingService.getByBorrowerId(borrowedUser.getId(), pageable);
        Page<BorrowingDetailed> page = borrowings.map(borrowingMapper::toDtoDetailed);

        return responseBuilder.success("Borrowing history", "fetched", new PagedResponse<>(page), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    @GetMapping("/report")
    @Operation(summary = "Get borrowing overdue report", description = "Generates a report of overdue borrowings, either for all users or a specific user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Overdue borrowing report fetched successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class)))
    })
    public ResponseEntity<LmsApiResponse<PagedResponse<BorrowingDetailed>>> getBorrowingReport(
            @RequestParam(required = false) Long borrowerId,
            @PageableDefault(size = 10, sort = "borrower") Pageable pageable) {

        Page<Borrowing> borrowings;
        if (borrowerId == null) {
            borrowings = borrowingService.getAllOverdue(pageable);
        } else {
            User reportUser = userService.getById(borrowerId);
            borrowings = borrowingService.getOverdueByBorrowerId(reportUser.getId(), pageable);
        }

        Page<BorrowingDetailed> page = borrowings.map(borrowingMapper::toDtoDetailed);
        borrowingService.printOverdueReport(page);

        return responseBuilder.success("Borrowing overdue report", "acquired", new PagedResponse<>(page),
                HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    @PostMapping("/{id}/excuse")
    @Operation(summary = "Excuse a returned overdue borrowing", description = "Excuses a returned borrowing that was overdue, effectively removing penalties.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Borrowing excused successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BorrowingDetailed.class))),
            @ApiResponse(responseCode = "404", description = "Not Found - Borrowing record not found", content = @Content),
            @ApiResponse(responseCode = "422", description = "Unprocessable entity - Borrowing is not excusable", content = @Content)

    })
    public ResponseEntity<LmsApiResponse<BorrowingDetailed>> excuseBorrowing(@PathVariable("id") long id) {

        Borrowing borrowing = borrowingService.getById(id);
        borrowingService.checkExcusable(borrowing);

        borrowing = borrowingService.excuseBorrowing(borrowing);

        return responseBuilder.success("Borrowing", "excused", borrowingMapper.toDtoDetailed(borrowing), HttpStatus.OK);
    }

}
