package com.planit.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PagedResponse<T> {

    private boolean success;
    private List<T> data;
    private Pagination pagination;
    private String timestamp;

    @Data
    @Builder
    public static class Pagination {
        private int page;
        private int pageSize;
        private long totalItems;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;
    }

    public static <T> PagedResponse<T> of(List<T> data,
                                          int page,
                                          int pageSize,
                                          long totalItems) {
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        return PagedResponse.<T>builder()
                .success(true)
                .data(data)
                .pagination(Pagination.builder()
                        .page(page)
                        .pageSize(pageSize)
                        .totalItems(totalItems)
                        .totalPages(totalPages)
                        .hasNext(page < totalPages)
                        .hasPrevious(page > 1)
                        .build())
                .timestamp(java.time.Instant.now().toString())
                .build();
    }
}
