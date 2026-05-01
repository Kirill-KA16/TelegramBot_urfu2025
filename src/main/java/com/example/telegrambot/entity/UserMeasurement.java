package com.example.telegrambot.entity;

import java.time.LocalDate;

public class UserMeasurement
{
    private Long id;
    private Long userId;
    private Double weight;
    private LocalDate measurementDate;

    public UserMeasurement() {}

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Double getWeight()
    {
        return weight;
    }

    public void setWeight(Double weight)
    {
        this.weight = weight;
    }

    public LocalDate getMeasurementDate()
    {
        return measurementDate;
    }

    public void setMeasurementDate(LocalDate measurementDate)
    {
        this.measurementDate = measurementDate;
    }
}
