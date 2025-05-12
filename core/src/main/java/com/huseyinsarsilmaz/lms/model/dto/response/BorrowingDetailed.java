package com.huseyinsarsilmaz.lms.model.dto.response;

import java.time.LocalDate;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class BorrowingDetailed extends BorrowingSimple {

    private LocalDate returnDate;

}