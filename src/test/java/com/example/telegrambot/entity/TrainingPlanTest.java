package com.example.telegrambot.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrainingPlanTest {

    @Test
    void testGettersAndSetters() {
        TrainingPlan plan = new TrainingPlan();
        plan.setId(1L);
        plan.setGoal("Похудение");
        plan.setFitnessLevel("Средний");
        plan.setEquipment("Зал");
        plan.setPlanDescription("Полноценная программа на 12 недель");
        plan.setExercises("Приседания 3x12, Жим лежа 4x10");

        assertEquals(1L, plan.getId());
        assertEquals("Похудение", plan.getGoal());
        assertEquals("Средний", plan.getFitnessLevel());
        assertEquals("Зал", plan.getEquipment());
        assertEquals("Полноценная программа на 12 недель", plan.getPlanDescription());
        assertEquals("Приседания 3x12, Жим лежа 4x10", plan.getExercises());
    }
}
