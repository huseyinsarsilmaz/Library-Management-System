package com.huseyinsarsilmaz.lms.model.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingHistory {
    private List<BorrowingSimple> active;
    private List<BorrowingDetailed> returned;

}
