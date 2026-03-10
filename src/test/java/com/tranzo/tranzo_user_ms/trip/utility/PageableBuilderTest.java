package com.tranzo.tranzo_user_ms.trip.utility;

import com.tranzo.tranzo_user_ms.trip.dto.SearchRequest;
import com.tranzo.tranzo_user_ms.trip.dto.SortCriteria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PageableBuilder Unit Tests")
class PageableBuilderTest {

    @Test
    @DisplayName("build with null sorts returns unsorted PageRequest")
    void build_nullSorts_returnsUnsorted() {
        SearchRequest request = new SearchRequest();
        request.setPage(1);
        request.setSize(10);
        request.setSorts(null);

        Pageable pageable = PageableBuilder.build(request);

        assertEquals(1, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertFalse(pageable.getSort().isSorted());
    }

    @Test
    @DisplayName("build with empty sorts returns unsorted PageRequest")
    void build_emptySorts_returnsUnsorted() {
        SearchRequest request = new SearchRequest();
        request.setPage(0);
        request.setSize(20);
        request.setSorts(List.of());

        Pageable pageable = PageableBuilder.build(request);

        assertEquals(0, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
    }

    @Test
    @DisplayName("build with sorts returns sorted PageRequest")
    void build_withSorts_returnsSorted() {
        SortCriteria sort = new SortCriteria();
        sort.setField("tripDestination");
        sort.setDirection("ASC");
        SearchRequest request = new SearchRequest();
        request.setPage(0);
        request.setSize(10);
        request.setSorts(List.of(sort));

        Pageable pageable = PageableBuilder.build(request);

        assertTrue(pageable.getSort().isSorted());
        Sort.Order order = pageable.getSort().iterator().next();
        assertEquals("tripDestination", order.getProperty());
        assertEquals(Sort.Direction.ASC, order.getDirection());
    }

    @Test
    @DisplayName("build with invalid sort field throws")
    void build_invalidSortField_throws() {
        SortCriteria sort = new SortCriteria();
        sort.setField("invalidField");
        sort.setDirection("DESC");
        SearchRequest request = new SearchRequest();
        request.setSorts(List.of(sort));

        assertThrows(IllegalArgumentException.class, () -> PageableBuilder.build(request));
    }
}
