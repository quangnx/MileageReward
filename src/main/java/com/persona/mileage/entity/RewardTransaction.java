package com.persona.mileage.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "k_reward_points")
public class RewardTransaction {

    public enum PointsType {
        earn, redeem
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "ride_id")
    private Long rideId;

    @Column(name = "points", precision = 38, scale = 2)
    private BigDecimal points;

    @Enumerated(EnumType.STRING)
    @Column(name = "points_type", nullable = false)
    private PointsType pointsType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "description")
    private String description;

    @Column(name = "type")
    private String type;

    // Constructors
    public RewardTransaction() {}

    public RewardTransaction(Long userId, Long rideId, BigDecimal points, PointsType pointsType, String description, String type) {
        this.userId = userId;
        this.rideId = rideId;
        this.points = points;
        this.pointsType = pointsType;
        this.createdAt = LocalDateTime.now();
        this.description = description;
        this.type = type;
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

    public PointsType getPointsType() {
        return pointsType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
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

    public void setPointsType(PointsType pointsType) {
        this.pointsType = pointsType;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(String type) {
        this.type = type;
    }
}
