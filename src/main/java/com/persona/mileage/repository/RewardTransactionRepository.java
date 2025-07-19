package com.persona.mileage.repository;

import com.persona.mileage.entity.RewardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RewardTransactionRepository extends JpaRepository<RewardTransaction, Long> {
    boolean existsByUserIdAndRideIdAndType(Long userId, Long rideId, String type);
}