package com.example.rbac.utils;

import org.junit.jupiter.api.Test;
from static org.junit.jupiter.api.Assertions.*;

public class ValidationUtilsTest {

    @Test
    void testIsValidUsername() {
        assertTrue(ValidationUtils.isValidUsername("user123"));
        assertFalse(ValidationUtils.isValidUsername("ab"));
        assertFalse(ValidationUtils.isValidUsername("invalid@user"));
        assertFalse(ValidationUtils.isValidUsername(""));
    }

    @Test
    void testIsValidEmail() {
        assertTrue(ValidationUtils.isValidEmail("test@example.com"));
        assertFalse(ValidationUtils.isValidEmail("invalid"));
        assertFalse(ValidationUtils.isValidEmail("test@"));
        assertFalse(ValidationUtils.isValidEmail(""));
    }

    @Test
    void testIsValidDate() {
        assertTrue(ValidationUtils.isValidDate("2026-02-28"));
        assertTrue(ValidationUtils.isValidDate("2026-02-28 12:30"));
        assertFalse(ValidationUtils.isValidDate("2026-13-01"));
        assertFalse(ValidationUtils.isValidDate(""));
    }

    @Test
    void testNormalizeString() {
        assertEquals("Test string", ValidationUtils.normalizeString("  Test   string  "));
        assertEquals("", ValidationUtils.normalizeString(null));
        assertEquals("Single word", ValidationUtils.normalizeString("Single word"));
    }

    @Test
    void testRequireNonEmpty() {
        ValidationUtils.requireNonEmpty("test", "Field");
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireNonEmpty("", "Field"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireNonEmpty("  ", "Field"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireNonEmpty(null, "Field"));
    }
}
