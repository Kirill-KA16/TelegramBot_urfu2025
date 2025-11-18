package com.example.telegrambot.model;

public class TrainingPlan {
    private Long id;
    private String goal;
    private String fitnessLevel;
    private String equipment;
    private String planDescription;
    private String exercises;

    public TrainingPlan() {}

    public TrainingPlan(String goal, String fitnessLevel, String equipment, 
                       String planDescription, String exercises) {
        this.goal = goal;
        this.fitnessLevel = fitnessLevel;
        this.equipment = equipment;
        this.planDescription = planDescription;
        this.exercises = exercises;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getFitnessLevel() { return fitnessLevel; }
    public void setFitnessLevel(String fitnessLevel) { this.fitnessLevel = fitnessLevel; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    public String getPlanDescription() { return planDescription; }
    public void setPlanDescription(String planDescription) { this.planDescription = planDescription; }

    public String getExercises() { return exercises; }
    public void setExercises(String exercises) { this.exercises = exercises; }

    @Override
    public String toString() {
        return "План тренировок:\n\n" +
               "Цель: " + goal + "\n" +
               "Уровень: " + fitnessLevel + "\n" +
               "Оборудование: " + equipment + "\n\n" +
               "Описание:\n" + planDescription + "\n\n" +
               "Упражнения:\n" + exercises;
    }
}