package com.persona.mileage.repository;

import com.persona.mileage.entity.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findByStatusAndCompletedAtBetween(String status, LocalDateTime start, LocalDateTime end);
}