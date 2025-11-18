package com.example.telegrambot.model;

import java.time.LocalDateTime;

public class User 
{
    private Long userId;
    private String goal;
    private String gender;
    private Integer age;
    private Double weight;
    private Double height;
    private String fitnessLevel;
    private String equipment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {}

    public User(Long userId, String goal, String gender, Integer age, 
                Double weight, Double height, String fitnessLevel, String equipment) 
    {
        this.userId = userId;
        this.goal = goal;
        this.gender = gender;
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.fitnessLevel = fitnessLevel;
        this.equipment = equipment;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }

    public String getFitnessLevel() { return fitnessLevel; }
    public void setFitnessLevel(String fitnessLevel) { this.fitnessLevel = fitnessLevel; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Double getBMI() 
    {
        if (height != null && weight != null && height > 0) 
        {
            return weight / ((height / 100) * (height / 100));
        }
        return null;
    }

    @Override
    public String toString() 
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Your profile:\n\n");
        
        if (goal != null) sb.append("Goal: ").append(goal).append("\n");
        if (gender != null) sb.append("Gender: ").append(gender).append("\n");
        if (age != null) sb.append("Age: ").append(age).append(" years\n");
        if (weight != null) sb.append("Weight: ").append(weight).append(" kg\n");
        if (height != null) sb.append("Height: ").append(height).append(" cm\n");
        if (fitnessLevel != null) sb.append("Level: ").append(fitnessLevel).append("\n");
        if (equipment != null) sb.append("Equipment: ").append(equipment).append("\n");
        
        Double bmi = getBMI();
        if (bmi != null) 
        {
            sb.append("BMI: ").append(String.format("%.1f", bmi)).append("\n");
        }
        
        return sb.toString();
    }
}