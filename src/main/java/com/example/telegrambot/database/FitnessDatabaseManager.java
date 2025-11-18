package com.example.telegrambot.database;

import com.example.telegrambot.model.User;
import com.example.telegrambot.model.UserMeasurement;
import com.example.telegrambot.model.TrainingPlan;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FitnessDatabaseManager 
{
    private static final String DB_URL = "jdbc:sqlite:fitness_bot.db";
    private static FitnessDatabaseManager instance;

    private FitnessDatabaseManager() 
    {
        initializeDatabase();
        initializeTrainingPlans();
    }

    public static synchronized FitnessDatabaseManager getInstance() 
    {
        if (instance == null) 
        {
            instance = new FitnessDatabaseManager();
        }
        return instance;
    }

    private void initializeDatabase() 
    {
        String[] createTables = {
            """
            CREATE TABLE IF NOT EXISTS users (
                user_id INTEGER PRIMARY KEY,
                goal TEXT,
                gender TEXT,
                age INTEGER,
                weight REAL,
                height REAL,
                fitness_level TEXT,
                equipment TEXT,
                created_at TEXT,
                updated_at TEXT
            )
            """,
            
            """
            CREATE TABLE IF NOT EXISTS user_measurements (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                weight REAL,
                measurement_date TEXT,
                FOREIGN KEY (user_id) REFERENCES users (user_id),
                UNIQUE(user_id, measurement_date)
            )
            """,
            
            """
            CREATE TABLE IF NOT EXISTS training_plans (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                goal TEXT,
                fitness_level TEXT,
                equipment TEXT,
                plan_description TEXT,
                exercises TEXT
            )
            """
        };

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) 
        {
            
            for (String sql : createTables) 
            {
                stmt.execute(sql);
            }
            System.out.println("Fitness database initialized successfully");
        } 
        catch (SQLException e) 
        {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    private void initializeTrainingPlans() 
    {
        List<TrainingPlan> defaultPlans = List.of(
            new TrainingPlan(
                "Weight loss", "Beginner", "No equipment",
                "Cardio workout for fat burning. Focus on interval exercises.",
                "1. Running in place - 3x30 sec\n2. Squats - 3x15\n3. Plank - 3x30 sec\n4. Burpees - 3x10"
            ),
            new TrainingPlan(
                "Mass gain", "Intermediate", "Dumbbells",
                "Strength training for muscle growth. Emphasis on basic exercises.",
                "1. Dumbbell press - 4x10\n2. Dumbbell squats - 4x12\n3. Dumbbell rows - 4x10\n4. Bicep curls - 3x15"
            ),
            new TrainingPlan(
                "Maintain fitness", "Beginner", "No equipment",
                "Balanced workout for maintaining muscle tone.",
                "1. Push-ups - 3x12\n2. Lunges - 3x10\n3. Crunches - 3x15\n4. Plank - 3x45 sec"
            )
        );

        for (TrainingPlan plan : defaultPlans) 
        {
            saveTrainingPlan(plan);
        }
    }

    // Methods for users
    public void saveUser(User user) 
    {
        String sql = """
            INSERT OR REPLACE INTO users 
            (user_id, goal, gender, age, weight, height, fitness_level, equipment, created_at, updated_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            
            pstmt.setLong(1, user.getUserId());
            pstmt.setString(2, user.getGoal());
            pstmt.setString(3, user.getGender());
            pstmt.setObject(4, user.getAge());
            pstmt.setObject(5, user.getWeight());
            pstmt.setObject(6, user.getHeight());
            pstmt.setString(7, user.getFitnessLevel());
            pstmt.setString(8, user.getEquipment());
            pstmt.setString(9, user.getCreatedAt().toString());
            pstmt.setString(10, user.getUpdatedAt().toString());
            
            pstmt.executeUpdate();
        } 
        catch (SQLException e) 
        {
            System.err.println("Error saving user: " + e.getMessage());
        }
    }

    public User getUser(Long userId) 
    {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) 
            {
                User user = new User();
                user.setUserId(rs.getLong("user_id"));
                user.setGoal(rs.getString("goal"));
                user.setGender(rs.getString("gender"));
                user.setAge(rs.getInt("age"));
                user.setWeight(rs.getDouble("weight"));
                user.setHeight(rs.getDouble("height"));
                user.setFitnessLevel(rs.getString("fitness_level"));
                user.setEquipment(rs.getString("equipment"));
                user.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
                user.setUpdatedAt(LocalDateTime.parse(rs.getString("updated_at")));
                return user;
            }
        } 
        catch (SQLException e) 
        {
            System.err.println("Error getting user: " + e.getMessage());
        }
        return null;
    }

    public void saveMeasurement(UserMeasurement measurement) 
    {
        String sql = """
            INSERT OR REPLACE INTO user_measurements 
            (user_id, weight, measurement_date) 
            VALUES (?, ?, ?)
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            
            pstmt.setLong(1, measurement.getUserId());
            pstmt.setDouble(2, measurement.getWeight());
            pstmt.setString(3, measurement.getMeasurementDate().toString());
            
            pstmt.executeUpdate();
        } 
        catch (SQLException e) 
        {
            System.err.println("Error saving measurement: " + e.getMessage());
        }
    }

    public List<UserMeasurement> getUserMeasurements(Long userId) 
    {
        List<UserMeasurement> measurements = new ArrayList<>();
        String sql = "SELECT * FROM user_measurements WHERE user_id = ? ORDER BY measurement_date DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) 
            {
                UserMeasurement measurement = new UserMeasurement();
                measurement.setId(rs.getLong("id"));
                measurement.setUserId(rs.getLong("user_id"));
                measurement.setWeight(rs.getDouble("weight"));
                measurement.setMeasurementDate(LocalDate.parse(rs.getString("measurement_date")));
                measurements.add(measurement);
            }
        } 
        catch (SQLException e) 
        {
            System.err.println("Error getting measurements: " + e.getMessage());
        }
        return measurements;
    }

    public void saveTrainingPlan(TrainingPlan plan) 
    {
        String checkSql = "SELECT id FROM training_plans WHERE goal = ? AND fitness_level = ? AND equipment = ?";
        String insertSql = """
            INSERT INTO training_plans (goal, fitness_level, equipment, plan_description, exercises)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) 
        {
            
            checkStmt.setString(1, plan.getGoal());
            checkStmt.setString(2, plan.getFitnessLevel());
            checkStmt.setString(3, plan.getEquipment());
            
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) 
            {
                // Plan doesn't exist, add it
                insertStmt.setString(1, plan.getGoal());
                insertStmt.setString(2, plan.getFitnessLevel());
                insertStmt.setString(3, plan.getEquipment());
                insertStmt.setString(4, plan.getPlanDescription());
                insertStmt.setString(5, plan.getExercises());
                insertStmt.executeUpdate();
            }
        } 
        catch (SQLException e) 
        {
            System.err.println("Error saving training plan: " + e.getMessage());
        }
    }

    public TrainingPlan getTrainingPlan(String goal, String fitnessLevel, String equipment) 
    {
        String sql = "SELECT * FROM training_plans WHERE goal = ? AND fitness_level = ? AND equipment = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            
            pstmt.setString(1, goal);
            pstmt.setString(2, fitnessLevel);
            pstmt.setString(3, equipment);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) 
            {
                TrainingPlan plan = new TrainingPlan();
                plan.setId(rs.getLong("id"));
                plan.setGoal(rs.getString("goal"));
                plan.setFitnessLevel(rs.getString("fitness_level"));
                plan.setEquipment(rs.getString("equipment"));
                plan.setPlanDescription(rs.getString("plan_description"));
                plan.setExercises(rs.getString("exercises"));
                return plan;
            }
        } 
        catch (SQLException e) 
        {
            System.err.println("Error getting training plan: " + e.getMessage());
        }
        return null;
    }

    public List<String> getAvailableGoals() 
    {
        List<String> goals = new ArrayList<>();
        String sql = "SELECT DISTINCT goal FROM training_plans";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) 
        {
            
            while (rs.next()) 
            {
                goals.add(rs.getString("goal"));
            }
        } 
        catch (SQLException e) 
        {
            System.err.println("Error getting goals: " + e.getMessage());
        }
        return goals;
    }
}