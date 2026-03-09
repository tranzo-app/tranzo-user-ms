package com.tranzo.tranzo_user_ms.trip.specification;

import com.tranzo.tranzo_user_ms.trip.dto.FilterCriteria;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class SpecificationBuilder<T> {
    public Specification<T> build(List<FilterCriteria> filters) {
        Specification<T> spec = Specification.where(null);
        if (filters == null || filters.isEmpty()) {
            return spec;
        }
        for (FilterCriteria filter : filters) {
            spec = spec.and(new GenericSpecification<>(filter));
        }
        return spec;
    }
}
