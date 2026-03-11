package com.tranzo.tranzo_user_ms.trip.specification;

import com.tranzo.tranzo_user_ms.trip.dto.FilterCriteria;
import com.tranzo.tranzo_user_ms.trip.utility.SearchFieldValidator;
import com.tranzo.tranzo_user_ms.trip.utility.SearchValueConverter;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class GenericSpecification<T> implements Specification<T> {

    private final FilterCriteria criteria;

    public GenericSpecification(FilterCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(
            Root<T> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb
    ) {

        String field = criteria.getField();

        // validate allowed fields
        SearchFieldValidator.validate(field);

        // detect field type
        Class<?> type = SearchFieldValidator.getFieldType(field);

        // convert request value to correct type
        Object value = SearchValueConverter.convert(criteria.getValue(), type);

        switch (criteria.getOperator()) {

            case EQUALS:
                return cb.equal(root.get(field), value);

            case CONTAINS:
                return cb.like(
                        cb.lower(root.get(field).as(String.class)),
                        "%" + value.toString().toLowerCase() + "%"
                );

            case GREATER_THAN:
                return cb.greaterThan(
                        root.get(field),
                        (Comparable) value
                );

            case LESS_THAN:
                return cb.lessThan(
                        root.get(field),
                        (Comparable) value
                );

            case BETWEEN:
                List<?> values = (List<?>) criteria.getValue();

                Comparable v1 = (Comparable)
                        SearchValueConverter.convert(values.get(0), type);

                Comparable v2 = (Comparable)
                        SearchValueConverter.convert(values.get(1), type);

                return cb.between(root.get(field), v1, v2);

            case IN:

                CriteriaBuilder.In<Object> inClause = cb.in(root.get(field));

                for (Object val : (List<?>) criteria.getValue()) {
                    inClause.value(
                            SearchValueConverter.convert(val, type)
                    );
                }

                return inClause;

            case IS_NULL:
                return cb.isNull(root.get(field));

            case IS_NOT_NULL:
                return cb.isNotNull(root.get(field));

            default:
                throw new IllegalArgumentException(
                        "Unsupported operator: " + criteria.getOperator()
                );
        }
    }
}