package com.persona.mileage.service;

import com.persona.mileage.entity.Ride;
import com.persona.mileage.entity.RewardTransaction;
import com.persona.mileage.repository.RideRepository;
import com.persona.mileage.repository.RewardTransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MileageRewardService {
    private static final Logger log = LoggerFactory.getLogger(MileageRewardService.class);
    private final RideRepository rideRepository;
    private final RewardTransactionRepository rewardRepo;

    public MileageRewardService(RideRepository rideRepository, RewardTransactionRepository rewardRepo) {
        this.rideRepository = rideRepository;
        this.rewardRepo = rewardRepo;
        log.info("Mileage job started at {}", LocalDateTime.now());
        // sendEmail("Mileage job started at " + LocalDateTime.now());
    }

    @Transactional
    public void processDailyRewards() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime start = yesterday.toLocalDate().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        List<Ride> rides = rideRepository.findByStatusAndCompletedAtBetween("COMPLETED", start, end);

        for (Ride ride : rides) {
            try {
                if (!rewardRepo.existsByUserIdAndRideIdAndType(ride.getUserId(), ride.getId(), "SELF")) {
                    rewardRepo.save(new RewardTransaction(null, ride.getUserId(), ride.getId(),
                        BigDecimal.valueOf(ride.getDistanceKm() * 0.01), "SELF", LocalDateTime.now()));
                }
            } catch (Exception e) {
                System.err.println("Error processing ride ID: " + ride.getId());
                e.printStackTrace();
            }
        }
    }
}