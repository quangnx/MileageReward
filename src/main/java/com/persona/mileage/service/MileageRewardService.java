package com.persona.mileage.service;

import com.persona.mileage.entity.RewardTransaction;
import com.persona.mileage.entity.Ride;
import com.persona.mileage.repository.RewardTransactionRepository;
import com.persona.mileage.repository.RideRepository;
import com.persona.mileage.repository.UserRepository;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MileageRewardService {
    private static final Logger log = LoggerFactory.getLogger(MileageRewardService.class);
    @Autowired
    private final RideRepository rideRepository;
    @Autowired
    private final RewardTransactionRepository rewardRepo;
    @Autowired
    private final UserRepository userRepository;

    public MileageRewardService(RideRepository rideRepository,
                                RewardTransactionRepository rewardTransactionRepository,
                                UserRepository userRepository) {
        this.rideRepository = rideRepository;
        this.rewardRepo = rewardTransactionRepository;
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "${job.mileage.reward.cron}")
    public void processDailyRewards() {
        log.info("▶️ Mileage job started at {}", LocalDateTime.now());

        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime start = yesterday.toLocalDate().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        List<Ride> rides = rideRepository.findByStatusAndCompletedAtBetween("completed", start, end);

        for (Ride ride : rides) {
            try {
                if (!rewardRepo.existsByUserIdAndRideIdAndType(ride.getUserId(), ride.getId(), "self")) {
                    BigDecimal points = BigDecimal.valueOf(ride.getDistanceKm() * 0.01);
                    rewardRepo.save(new RewardTransaction(
                            null,
                            ride.getUserId(),
                            ride.getId(),
                            points,
                            "self",
                            LocalDateTime.now()
                    ));
                }

                Long referrerId = userRepository.findReferrerIdByUserId(ride.getUserId());
                if (referrerId != null &&
                        !rewardRepo.existsByUserIdAndRideIdAndType(referrerId, ride.getId(), "referral")) {
                    try {
                        rewardRepo.save(new RewardTransaction(
                                ride.getUserId(),
                                referrerId,
                                ride.getId(),
                                BigDecimal.valueOf(ride.getDistanceKm() * 0.005),
                                "referral",
                                LocalDateTime.now()
                        ));
                    } catch (Exception e) {
                        log.error("Error processing ride ID: {}", ride.getId(), e);
                    }

                }

            } catch (Exception e) {
                System.err.println("Error processing ride ID: " + ride.getId());
                e.printStackTrace();
            }
        }
    }
}