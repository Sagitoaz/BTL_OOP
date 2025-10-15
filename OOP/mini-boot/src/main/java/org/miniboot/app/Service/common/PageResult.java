package org.miniboot.app.Service.common;

import java.util.List;

public class PageResult<T> {
    public final List<T> items;
    public final int page;
    public final int size;
    public final long totalElements;
    public final int totalPages;

    public PageResult(List<T> items, int page, int size, long totalElements) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.max(1, Math.ceil(totalElements / (double) size));
    }
}
