package com.huseyinsarsilmaz.lmsr.model.dto.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkReturnRequest {

    private List<Long> borrowingIds;
}