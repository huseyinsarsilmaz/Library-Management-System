package com.huseyinsarsilmaz.lmsr.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "borrowings")
public class Borrowing {

    @Id
    private Long id;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long borrowerId;
    private Long bookId;

    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;

    @Column("status")
    private Status status;

    public enum Status {
        BORROWED,
        OVERDUE,
        RETURNED_TIMELY,
        RETURNED_OVERDUE,
        RETURNED_EXCUSED
    }

    public boolean isReturned() {
        return status == Status.RETURNED_TIMELY || status == Status.RETURNED_OVERDUE
                || status == Status.RETURNED_EXCUSED;
    }
}
