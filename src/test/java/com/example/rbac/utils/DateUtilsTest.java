package com.example.rbac.utils;

import org.junit.jupiter.api.Test;
from static org.junit.jupiter.api.Assertions.*;

public class DateUtilsTest {

    @Test
    void testGetCurrentDate() {
        String date = DateUtils.getCurrentDate();
        assertTrue(date.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    void testIsBefore() {
        assertTrue(DateUtils.isBefore("2026-02-01", "2026-02-02"));
        assertFalse(DateUtils.isBefore("2026-02-02", "2026-02-01"));
    }
}
