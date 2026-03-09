package com.tranzo.tranzo_user_ms.trip.utility;

import com.tranzo.tranzo_user_ms.trip.dto.SearchRequest;
import com.tranzo.tranzo_user_ms.trip.dto.SortCriteria;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public class PageableBuilder {
    public static Pageable build(SearchRequest request) {
        if (request.getSorts() == null || request.getSorts().isEmpty()) {
            return PageRequest.of(request.getPage(), request.getSize());
        }
        List<Sort.Order> orders = new ArrayList<>();
        for (SortCriteria sort : request.getSorts()) {
            SearchFieldValidator.validate(sort.getField());
            orders.add(new Sort.Order(
                    Sort.Direction.fromString(sort.getDirection()),
                    sort.getField()
            ));
        }
        return PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(orders)
        );
    }
}
