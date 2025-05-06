package com.huseyinsarsilmaz.lms.model.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Data;

@Data
public class PagedResponse<T> {
    private List<T> items;

    private int page;
    private int size;
    private long totalItems;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public PagedResponse(Page<T> page) {
        this.items = page.getContent();
        this.page = page.getNumber();
        this.size = page.getSize();
        this.totalItems = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.hasNext = page.hasNext();
        this.hasPrevious = page.hasPrevious();
    }
}
