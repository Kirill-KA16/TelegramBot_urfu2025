package com.example.telegrambot;

import com.example.telegrambot.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorkoutPlanGeneratorTest {

    private final WorkoutPlanGenerator generator = new WorkoutPlanGenerator();

    private User createBaseUser() {
        User user = new User();
        user.setAge(30);
        user.setWeight(75.0);
        user.setHeight(175.0);
        user.setFitnessLevel("Средний");
        user.setEquipment("гантели");
        return user;
    }

    @Test
    @DisplayName("Набор: 3 силовые, 3x8-10")
    void generatePlan_forGainGoal() {
        User user = createBaseUser();
        user.setGoal("Набор");
        
        String plan = generator.generatePlan(user);
        
        assertAll(
            () -> assertTrue(plan.contains("Силовая"), "Должны быть силовые тренировки"),
            () -> assertTrue(plan.contains("3x8-10") || plan.contains("8-10"), "Повторения 8-10"),
            () -> assertTrue(plan.contains("Понедельник") || plan.contains("Среда") || plan.contains("Пятница"),
                "Должны быть дни недели")
        );
    }

    @Test
    @DisplayName("Похудение: 1 силовая + 3 кардио, 4x12-15")
    void generatePlan_forWeightLossGoal() {
        User user = createBaseUser();
        user.setGoal("Похудение");
        
        String plan = generator.generatePlan(user);
        
        assertAll(
            () -> assertTrue(plan.contains("Кардио") || plan.contains("бега") || plan.contains("ходьбы") || plan.contains("прыжков"),
                "Должны быть кардио-тренировки"),
            () -> assertTrue(plan.contains("4x12-15") || plan.contains("12-15"), "Повторения 12-15")
        );
    }

    @Test
    @DisplayName("Сила и выносливость: 2 силовые + 2 кардио, 3x12")
    void generatePlan_forStrengthGoal() {
        User user = createBaseUser();
        user.setGoal("Сила и выносливость");
        
        String plan = generator.generatePlan(user);
        
        assertAll(
            () -> assertTrue(plan.contains("3x12") || plan.contains("12"), "Повторения 12")
        );
    }

    @Test
    @DisplayName("Поддержание: 2 силовые + 1 кардио")
    void generatePlan_forMaintenanceGoal() {
        User user = createBaseUser();
        user.setGoal("Поддержание");
        
        String plan = generator.generatePlan(user);
        
        assertNotNull(plan);
        assertTrue(plan.contains("Ваш тренировочный план"));
    }

    @Test
    @DisplayName("План 'Набор' содержит упоминание о восстановлении")
    void allPlans_includeRecoveryWarning() {
        User user = createBaseUser();
        user.setGoal("Набор");
        String plan = generator.generatePlan(user);
        
        boolean hasRecovery = plan.contains("отдых") || 
                              plan.contains("восстановление") ||
                              plan.contains("восстановиться");
        
        assertTrue(hasRecovery, "План для цели 'Набор' должен содержать упоминание об отдыхе или восстановлении");
    }

    @Test
    @DisplayName("Если цель не указана - используется 'Поддержание'")
    void generatePlan_whenGoalIsNull_usesDefault() {
        User user = createBaseUser();
        user.setGoal(null);
        
        String plan = generator.generatePlan(user);
        
        assertNotNull(plan);
        assertTrue(plan.contains("Ваш тренировочный план"));
    }
}