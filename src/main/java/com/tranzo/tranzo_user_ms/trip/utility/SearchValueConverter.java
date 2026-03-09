package com.tranzo.tranzo_user_ms.trip.utility;

import java.time.LocalDate;

public class SearchValueConverter {
    public static Object convert(Object value, Class<?> type) {
        if (value == null) return null;
        if (type.equals(Integer.class)) {
            return Integer.valueOf(value.toString());
        }
        if (type.equals(Long.class)) {
            return Long.valueOf(value.toString());
        }
        if (type.equals(LocalDate.class)) {
            return LocalDate.parse(value.toString());
        }
        return value.toString();
    }
}
