package com.tranzo.tranzo_user_ms.trip.utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SearchFieldValidator Unit Tests")
class SearchFieldValidatorTest {

    @Test
    @DisplayName("validate allows tripDestination")
    void validate_tripDestination_doesNotThrow() {
        assertDoesNotThrow(() -> SearchFieldValidator.validate("tripDestination"));
    }

    @Test
    @DisplayName("validate allows tripStartDate")
    void validate_tripStartDate_doesNotThrow() {
        assertDoesNotThrow(() -> SearchFieldValidator.validate("tripStartDate"));
    }

    @Test
    @DisplayName("validate allows tripEndDate")
    void validate_tripEndDate_doesNotThrow() {
        assertDoesNotThrow(() -> SearchFieldValidator.validate("tripEndDate"));
    }

    @Test
    @DisplayName("validate disallows unknown field")
    void validate_unknownField_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> SearchFieldValidator.validate("invalidField"));
        assertTrue(ex.getMessage().contains("Filtering not allowed on field"));
        assertTrue(ex.getMessage().contains("invalidField"));
    }

    @Test
    @DisplayName("getFieldType returns correct type for allowed fields")
    void getFieldType_returnsCorrectType() {
        assertEquals(String.class, SearchFieldValidator.getFieldType("tripDestination"));
        assertEquals(String.class, SearchFieldValidator.getFieldType("tripStartDate"));
        assertEquals(String.class, SearchFieldValidator.getFieldType("tripEndDate"));
    }

    @Test
    @DisplayName("getFieldType returns null for unknown field")
    void getFieldType_unknownField_returnsNull() {
        assertNull(SearchFieldValidator.getFieldType("unknown"));
    }
}
