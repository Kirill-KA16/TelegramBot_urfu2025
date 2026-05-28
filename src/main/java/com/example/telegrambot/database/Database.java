package com.example.telegrambot.database;

import com.example.telegrambot.entity.User;
import com.example.telegrambot.entity.UserMeasurement;
import com.example.telegrambot.entity.TrainingPlan;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlite3.SQLitePlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class Database
{
    private static Database instance;
    private final Jdbi jdbi;

    private Database()
    {
        String home = System.getProperty("user.home");
        String dbDir = home + "/.fitnessbot";
        String dbPath = dbDir + "/fitness_bot.db";

        try
        {
            Files.createDirectories(Paths.get(dbDir));
        }
        catch (Exception e)
        {
            System.err.println("Не удалось создать папку для БД: " + dbDir);
        }

        String url = "jdbc:sqlite:" + dbPath;
        this.jdbi = Jdbi.create(url)
                        .installPlugin(new SQLitePlugin())
                        .installPlugin(new SqlObjectPlugin());

        createTables();
    }

    public static synchronized Database getInstance()
    {
        if (instance == null)
        {
            instance = new Database();
        }
        return instance;
    }

    private void createTables()
    {
        jdbi.useHandle(handle ->
        {
            handle.execute(
                """
                CREATE TABLE IF NOT EXISTS users (
                    user_id BIGINT PRIMARY KEY,
                    goal TEXT,
                    gender TEXT,
                    age INTEGER,
                    weight REAL,
                    height REAL,
                    fitness_level TEXT,
                    equipment TEXT,
                    current_workout_plan TEXT,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
                """
            );

            try
            {
                handle.execute("ALTER TABLE users ADD COLUMN current_workout_plan TEXT");
                System.out.println("Колонка current_workout_plan успешно добавлена.");
            }
            catch (Exception e)
            {
                if (e.getMessage() != null && e.getMessage().contains("duplicate column name"))
                {
                    System.out.println("Колонка current_workout_plan уже существует.");
                }
                else
                {
                    System.err.println("Ошибка при добавлении колонки current_workout_plan:");
                    e.printStackTrace();
                }
            }

            handle.execute(
                """
                CREATE TABLE IF NOT EXISTS user_measurements (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id BIGINT NOT NULL,
                    weight REAL NOT NULL,
                    measurement_date DATE NOT NULL DEFAULT CURRENT_DATE,
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                )
                """
            );

            handle.execute(
                """
                CREATE TABLE IF NOT EXISTS training_plans (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    goal TEXT NOT NULL,
                    fitness_level TEXT NOT NULL,
                    equipment TEXT NOT NULL,
                    plan_description TEXT,
                    exercises TEXT NOT NULL
                )
                """
            );
        });
    }

    public void updateUser(User user)
    {
        jdbi.useHandle(h -> h.execute(
            """
            INSERT INTO users (user_id, goal, gender, age, weight, height, fitness_level, equipment)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(user_id) DO UPDATE SET
                goal = excluded.goal,
                gender = excluded.gender,
                age = excluded.age,
                weight = excluded.weight,
                height = excluded.height,
                fitness_level = excluded.fitness_level,
                equipment = excluded.equipment,
                updated_at = CURRENT_TIMESTAMP
            """,
            user.getUserId(),
            user.getGoal(),
            user.getGender(),
            user.getAge(),
            user.getWeight(),
            user.getHeight(),
            user.getFitnessLevel(),
            user.getEquipment()
        ));
    }

    public Optional<User> getUser(long userId)
    {
        return jdbi.withHandle(h ->
            h.createQuery("SELECT * FROM users WHERE user_id = ?")
             .bind(0, userId)
             .mapToBean(User.class)
             .findFirst()
        );
    }

    public void addMeasurement(long userId, double weight)
    {
        jdbi.useHandle(h ->
            h.execute("INSERT INTO user_measurements (user_id, weight) VALUES (?, ?)", userId, weight)
        );
    }

    public List<UserMeasurement> getMeasurements(long userId)
    {
        return jdbi.withHandle(h ->
            h.createQuery(
                """
                SELECT
                    id,
                    user_id AS userId,
                    weight,
                    measurement_date AS measurementDate
                FROM user_measurements
                WHERE user_id = ?
                ORDER BY measurement_date DESC
                """
            )
            .bind(0, userId)
            .mapToBean(UserMeasurement.class)
            .list()
        );
    }

    public void saveWorkoutPlan(long userId, String planText)
    {
        jdbi.useHandle(h -> h.execute(
            "UPDATE users SET current_workout_plan = ? WHERE user_id = ?",
            planText,
            userId
        ));
    }

    public Optional<String> getWorkoutPlan(long userId)
    {
        return jdbi.withHandle(h ->
            h.createQuery("SELECT current_workout_plan FROM users WHERE user_id = ?")
             .bind(0, userId)
             .mapTo(String.class)
             .findFirst()
        );
    }
}
