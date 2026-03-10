package com.tranzo.tranzo_user_ms.trip.dto;

import lombok.Data;

import java.util.List;

@Data
public class SearchRequest {
    private List<FilterCriteria> filters;
    private List<SortCriteria> sorts;
    private String globalSearch;
    private int page = 0;
    private int size = 20;
}
