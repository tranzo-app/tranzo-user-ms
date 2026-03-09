package com.tranzo.tranzo_user_ms.trip.utility;

import com.tranzo.tranzo_user_ms.trip.model.TripEntity;

import java.time.LocalDate;
import java.util.Map;

public class SearchFieldValidator {
//    private static final Map<String, Class<?>> ALLOWED_FIELDS = Map.of(
//            "tripDestination", String.class,
//            "tripStartDate", String.class,
//            "tripEndDate", String.class
//    );

    public static void validate(String field) {

//        if (!ALLOWED_FIELDS.containsKey(field)) {
//            throw new IllegalArgumentException(
//                    "Filtering not allowed on field: " + field
//            );
//        }
    }

    public static Class<?> getFieldType(String field) {
        try {
            return TripEntity.class.getDeclaredField(field).getType();
        } catch (Exception e) {
            throw new RuntimeException("Invalid field: " + field);
        }
    }

//    public static Class<?> getFieldType(String field) {
//        return ALLOWED_FIELDS.get(field);
//    }
}
