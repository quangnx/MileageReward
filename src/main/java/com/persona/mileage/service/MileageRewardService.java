package com.persona.mileage.service;

import com.persona.mileage.entity.RewardTransaction;
import com.persona.mileage.entity.Ride;
import com.persona.mileage.repository.RewardTransactionRepository;
import com.persona.mileage.repository.RideRepository;
import com.persona.mileage.repository.UserRepository;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MileageRewardService {
    private static final Logger log = LoggerFactory.getLogger(MileageRewardService.class);
    private final RideRepository rideRepository;
    private final RewardTransactionRepository rewardRepo;
    private final UserRepository userRepository;

    public MileageRewardService(RideRepository rideRepository, RewardTransactionRepository rewardRepo,UserRepository userRepository) {
        this.rideRepository = rideRepository;
        this.rewardRepo = rewardRepo;
        this.userRepository = userRepository;
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
                    rewardRepo.save(new RewardTransaction(null, ride.getUserId(),
                            BigDecimal.valueOf(ride.getDistanceKm() * 0.01), "SELF"));
                }
                UserRepository userRepository = null;
                RewardTransactionRepository rewardTransactionRepository = null;
                Long referrerId = userRepository.findReferrerIdByUserId(ride.getUserId());
                if (referrerId != null && !rewardTransactionRepository.existsByUserIdAndRideIdAndType(referrerId, ride.getId(), "REFERRAL")) {
                    rewardTransactionRepository.save(new RewardTransaction(
                            referrerId, ride.getId(),
                            BigDecimal.valueOf(ride.getDistanceKm() * 0.001),
                            "REFERRAL"
                    ));
                }
            } catch (Exception e) {
                System.err.println("Error processing ride ID: " + ride.getId());
                e.printStackTrace();
            }
        }
    }
}