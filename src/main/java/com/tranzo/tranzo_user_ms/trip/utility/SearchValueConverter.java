package com.tranzo.tranzo_user_ms.trip.utility;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SearchValueConverter {
    public static Object convert(Object value, Class<?> type) {
        if (value == null) {
            return null;
        }
        if (type.equals(String.class)) {
            return value.toString();
        }
        if (type.equals(Integer.class)) {
            return Integer.valueOf(value.toString());
        }
        if (type.equals(Long.class)) {
            return Long.valueOf(value.toString());
        }
        if (type.equals(Double.class)) {
            return Double.valueOf(value.toString());
        }
        if (type.equals(LocalDate.class)) {
            return LocalDate.parse(value.toString());
        }
        if (type.equals(LocalDateTime.class)) {
            return LocalDateTime.parse(value.toString());
        }
        return value;
    }
}
