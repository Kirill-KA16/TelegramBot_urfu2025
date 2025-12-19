package com.example.telegrambot.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testGettersAndSetters() {
        User user = new User();
        user.setUserId(100L);
        user.setGoal("Набор массы");
        user.setGender("Женский");
        user.setAge(25);
        user.setWeight(60.0);
        user.setHeight(165.0);
        user.setFitnessLevel("Новичок");
        user.setEquipment("Дома без инвентаря");
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        assertEquals(100L, user.getUserId());
        assertEquals("Набор массы", user.getGoal());
        assertEquals("Женский", user.getGender());
        assertEquals(25, user.getAge());
        assertEquals(60.0, user.getWeight());
        assertEquals(165.0, user.getHeight());
        assertEquals("Новичок", user.getFitnessLevel());
        assertEquals("Дома без инвентаря", user.getEquipment());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
    }
}
