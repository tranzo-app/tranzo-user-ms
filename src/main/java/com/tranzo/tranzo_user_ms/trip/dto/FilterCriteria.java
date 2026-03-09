package com.tranzo.tranzo_user_ms.trip.dto;

import com.tranzo.tranzo_user_ms.trip.enums.FilterOperator;
import lombok.Data;

@Data
public class FilterCriteria {
    private String field;
    private FilterOperator operator;
    private Object value;
}
