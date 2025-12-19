package com.example.telegrambot.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserMeasurementTest {

    @Test
    void testGettersAndSetters() {
        UserMeasurement m = new UserMeasurement();
        m.setId(1L);
        m.setUserId(123L);
        m.setWeight(72.5);
        m.setMeasurementDate(LocalDate.of(2025, 12, 19));

        assertEquals(1L, m.getId());
        assertEquals(123L, m.getUserId());
        assertEquals(72.5, m.getWeight());
        assertEquals(LocalDate.of(2025, 12, 19), m.getMeasurementDate());
    }
}
