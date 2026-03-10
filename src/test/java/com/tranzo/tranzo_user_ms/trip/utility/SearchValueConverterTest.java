package com.tranzo.tranzo_user_ms.trip.utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SearchValueConverter Unit Tests")
class SearchValueConverterTest {

    @Test
    @DisplayName("convert null returns null")
    void convert_null_returnsNull() {
        assertNull(SearchValueConverter.convert(null, String.class));
    }

    @Test
    @DisplayName("convert to Integer")
    void convert_toInteger() {
        assertEquals(42, SearchValueConverter.convert("42", Integer.class));
        assertEquals(0, SearchValueConverter.convert(0, Integer.class));
    }

    @Test
    @DisplayName("convert to Long")
    void convert_toLong() {
        assertEquals(100L, SearchValueConverter.convert("100", Long.class));
    }

    @Test
    @DisplayName("convert to LocalDate")
    void convert_toLocalDate() {
        Object result = SearchValueConverter.convert("2025-03-09", LocalDate.class);
        assertEquals(LocalDate.of(2025, 3, 9), result);
    }

    @Test
    @DisplayName("convert to String returns toString")
    void convert_toString_returnsToString() {
        assertEquals("hello", SearchValueConverter.convert("hello", String.class));
        assertEquals("123", SearchValueConverter.convert(123, String.class));
    }
}
