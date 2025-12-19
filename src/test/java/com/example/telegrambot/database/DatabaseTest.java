package com.example.telegrambot.database;

import com.example.telegrambot.entity.User;
import com.example.telegrambot.entity.UserMeasurement;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseTest {

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
    void getInstance_returnsSingleton() {
        Database db1 = Database.getInstance();
        Database db2 = Database.getInstance();

        assertSame(db1, db2);
    }

    @Test
    void updateUser_and_getUser_shouldPersistUser() {
        Database db = Database.getInstance();

        User user = new User(1L);
        user.setGoal("Похудение");
        user.setGender("М");
        user.setAge(30);
        user.setWeight(85.5);
        user.setHeight(180.0);
        user.setFitnessLevel("BEGINNER");
        user.setEquipment("NONE");

        db.updateUser(user);

        Optional<User> fromDb = db.getUser(1L);

        assertTrue(fromDb.isPresent());
        assertEquals("Похудение", fromDb.get().getGoal());
        assertEquals(30, fromDb.get().getAge());
        assertEquals(85.5, fromDb.get().getWeight());
    }

    @Test
    void updateUser_shouldUpdateExistingUser() {
        Database db = Database.getInstance();

        User user = new User(2L);
        user.setGoal("Набор массы");
        user.setAge(25);
        db.updateUser(user);

        user.setGoal("Сушка");
        user.setAge(26);
        db.updateUser(user);

        User updated = db.getUser(2L).orElseThrow();

        assertEquals("Сушка", updated.getGoal());
        assertEquals(26, updated.getAge());
    }

    @Test
    void getUser_shouldReturnEmptyIfNotExists() {
        Database db = Database.getInstance();

        Optional<User> user = db.getUser(999L);

        assertTrue(user.isEmpty());
    }

    @Test
    void addMeasurement_and_getMeasurements_shouldWork() {
        Database db = Database.getInstance();

        User user = new User(3L);
        db.updateUser(user);

        db.addMeasurement(3L, 90.0);
        db.addMeasurement(3L, 89.5);

        List<UserMeasurement> measurements = db.getMeasurements(3L);

        assertEquals(2, measurements.size());
        assertEquals(89.5, measurements.get(0).getWeight());
        assertEquals(90.0, measurements.get(1).getWeight());
    }

    @Test
    void getMeasurements_shouldReturnEmptyListIfNone() {
        Database db = Database.getInstance();

        User user = new User(4L);
        db.updateUser(user);

        List<UserMeasurement> measurements = db.getMeasurements(4L);

        assertNotNull(measurements);
        assertTrue(measurements.isEmpty());
    }
}
