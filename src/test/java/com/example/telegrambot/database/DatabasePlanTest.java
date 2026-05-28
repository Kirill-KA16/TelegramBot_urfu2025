package com.example.telegrambot.database;

import com.example.telegrambot.entity.User;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabasePlanTest {

    private Path tempHomeDir;

    @BeforeAll
    void setupHomeDir() throws Exception {
        tempHomeDir = Files.createTempDirectory("fitnessbot-test");
        System.setProperty("user.home", tempHomeDir.toString());
    }

    @BeforeEach
    void resetSingleton() throws Exception {
        Field instanceField = Database.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    @AfterAll
    void cleanup() throws Exception {
        Files.walk(tempHomeDir)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(path -> path.toFile().delete());
    }

    @Test
    @DisplayName("saveWorkoutPlan и getWorkoutPlan - должны сохранять и получать план")
    void saveWorkoutPlan_and_getWorkoutPlan_shouldWork() {
        Database db = Database.getInstance();

        User user = new User(1L);
        user.setAge(30);
        db.updateUser(user);

        String planText = "Ваш тренировочный план на неделю\nПонедельник: Приседания 3x10";

        db.saveWorkoutPlan(1L, planText);
        Optional<String> savedPlan = db.getWorkoutPlan(1L);

        assertTrue(savedPlan.isPresent());
        assertEquals(planText, savedPlan.get());
    }

    @Test
    @DisplayName("saveWorkoutPlan - должен перезаписывать существующий план")
    void saveWorkoutPlan_shouldOverwriteExistingPlan() {
        Database db = Database.getInstance();

        User user = new User(2L);
        db.updateUser(user);

        String firstPlan = "Первый план";
        String secondPlan = "Второй план (обновлённый)";

        db.saveWorkoutPlan(2L, firstPlan);
        db.saveWorkoutPlan(2L, secondPlan);

        Optional<String> savedPlan = db.getWorkoutPlan(2L);
        assertTrue(savedPlan.isPresent());
        assertEquals(secondPlan, savedPlan.get());
    }

    @Test
    @DisplayName("getWorkoutPlan - для пользователя без плана возвращает Optional.empty()")
    void getWorkoutPlan_whenNoPlan_returnsEmpty() {
        Database db = Database.getInstance();

        User user = new User(3L);
        db.updateUser(user);

        db.saveWorkoutPlan(3L, null);

        Optional<String> plan = db.getWorkoutPlan(3L);
        assertFalse(plan.isPresent());
    }

    @Test
    @DisplayName("saveWorkoutPlan - сохранение null должно очистить план")
    void saveWorkoutPlan_savingNull_shouldClearPlan() {
        Database db = Database.getInstance();

        User user = new User(4L);
        db.updateUser(user);

        db.saveWorkoutPlan(4L, "Какой-то план");
        db.saveWorkoutPlan(4L, null);

        Optional<String> savedPlan = db.getWorkoutPlan(4L);
        assertFalse(savedPlan.isPresent());
    }

    @Test
    @DisplayName("getWorkoutPlan - разные пользователи имеют разные планы")
    void getWorkoutPlan_differentUsers_haveDifferentPlans() {
        Database db = Database.getInstance();

        User user1 = new User(5L);
        User user2 = new User(6L);
        db.updateUser(user1);
        db.updateUser(user2);

        String plan1 = "План пользователя 1";
        String plan2 = "План пользователя 2";

        db.saveWorkoutPlan(5L, plan1);
        db.saveWorkoutPlan(6L, plan2);

        Optional<String> savedPlan1 = db.getWorkoutPlan(5L);
        Optional<String> savedPlan2 = db.getWorkoutPlan(6L);

        assertEquals(plan1, savedPlan1.get());
        assertEquals(plan2, savedPlan2.get());
    }
}
