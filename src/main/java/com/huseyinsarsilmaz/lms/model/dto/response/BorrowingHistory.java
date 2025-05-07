package com.huseyinsarsilmaz.lms.model.dto.response;

import java.util.List;

import lombok.Data;

@Data
public class BorrowingHistory {
    private final List<BorrowingSimple> active;
    private final List<BorrowingDetailed> returned;

}
