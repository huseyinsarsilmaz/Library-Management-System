package com.huseyinsarsilmaz.lmsr.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huseyinsarsilmaz.lmsr.model.dto.request.BulkReturnRequest;
import com.huseyinsarsilmaz.lmsr.model.entity.Borrowing;
import com.huseyinsarsilmaz.lmsr.service.BorrowingService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/borrowings")
@RequiredArgsConstructor
public class BulkReturnController {

    private final BorrowingService borrowingService;

    @PostMapping("/bulk-return")
    public Flux<Borrowing> bulkReturn(@RequestBody BulkReturnRequest bulkReturnRequest) {
        return borrowingService.returnBorrowings(bulkReturnRequest.getBorrowingIds())
                .onErrorResume(e -> Flux.empty());
    }
}