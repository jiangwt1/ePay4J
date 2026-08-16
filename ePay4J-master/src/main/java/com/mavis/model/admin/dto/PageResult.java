package com.mavis.model.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private List<T> records;
    private long total;
    private long page;
    private long size;
}
