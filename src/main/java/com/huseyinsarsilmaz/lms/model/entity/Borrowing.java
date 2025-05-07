package com.huseyinsarsilmaz.lms.model.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "borrowings")
public class Borrowing extends LmsEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "borrower_id")
    private User borrower;

    @ManyToOne(optional = false)
    @JoinColumn(name = "book_id")
    private Book book;

    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        BORROWED,
        OVERDUE,
        RETURNED_TIMELY,
        RETURNED_OVERDUE
    }

    public boolean isReturned() {
        return status == Status.RETURNED_TIMELY || status == Status.RETURNED_OVERDUE;
    }
}
