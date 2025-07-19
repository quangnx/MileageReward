package com.persona.mileage.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class RewardTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long rideId;
    private BigDecimal points;
    private String type;
    private LocalDateTime createdAt;

    // Constructors

    public RewardTransaction() {}

    public RewardTransaction(Long id, Long userId, Long rideId, BigDecimal points, String type, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.rideId = rideId;
        this.points = points;
        this.type = type;
        this.createdAt = createdAt;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getRideId() {
        return rideId;
    }

    public BigDecimal getPoints() {
        return points;
    }

    public String getType() {
        return type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public void setPoints(BigDecimal points) {
        this.points = points;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
