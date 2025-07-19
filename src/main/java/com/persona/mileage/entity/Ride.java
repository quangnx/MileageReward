package com.persona.mileage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private double distanceKm;
    private String status;
    private LocalDateTime completedAt;

    // Constructors
    public Ride() {}

    public Ride(Long userId, double distanceKm, String status, LocalDateTime completedAt) {
        this.userId = userId;
        this.distanceKm = distanceKm;
        this.status = status;
        this.completedAt = completedAt;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) { this.userId = userId; }

    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public void setStatus(String status) { this.status = status; }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
