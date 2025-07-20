package com.persona.mileage.service;

import com.persona.mileage.entity.RewardTransaction;
import com.persona.mileage.entity.Ride;
import com.persona.mileage.repository.RewardTransactionRepository;
import com.persona.mileage.repository.RideRepository;
import com.persona.mileage.repository.UserRepository;
import jakarta.transaction.Transactional;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class MileageRewardService {
    private static final Logger log = LoggerFactory.getLogger(MileageRewardService.class);
    @Autowired
    private final RideRepository rideRepository;
    @Autowired
    private final RewardTransactionRepository rewardRepo;
    @Autowired
    private final UserRepository userRepository;

    @Value("${job.mileage.batch.size:500}")
    private int batchSize;

    public MileageRewardService(RideRepository rideRepository,
                                RewardTransactionRepository rewardTransactionRepository,
                                UserRepository userRepository) {
        this.rideRepository = rideRepository;
        this.rewardRepo = rewardTransactionRepository;
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "${job.mileage.reward.cron}")
    /**
     * Scheduled job entrypoint (can be called by @Scheduled from another class).
     */
    public void processDailyRewards() {
        log.info("MileageRewardService: Start reward processing at {}", LocalDateTime.now());

        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime start = yesterday.toLocalDate().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        int page = 0;
        Page<Ride> ridePage;
        do {
            ridePage = rideRepository.findByStatusAndCompletedAtBetweenAndIsRewardFalse(
                    "completed", start, end, PageRequest.of(page, batchSize));
            List<Ride> rides = ridePage.getContent();
            if (!rides.isEmpty()) {
                processBatch(rides);
                page++;
            }
        } while (!ridePage.isEmpty());

        log.info("MileageRewardService: Finished reward processing at {}", LocalDateTime.now());
    }

    /**
     * Process a batch of rides for reward calculation & saving.
     * Ensures transactional integrity and idempotency.
     */
    @Transactional
    public void processBatch(List<Ride> rides) {
        for (Ride ride : rides) {
            try {
                // 1. Reward for customer (idempotent)
                if (!rewardRepo.existsByUserIdAndRideIdAndType(ride.getUserId(), ride.getId(), "self")) {
                    BigDecimal points = BigDecimal.valueOf(ride.getDistanceKm() * 0.01);
                    RewardTransaction selfReward = new RewardTransaction(
                            ride.getUserId(),
                            ride.getId(),
                            points,
                            RewardTransaction.PointsType.earn,
                            "Mileage reward",
                            "self"
                    );
                    rewardRepo.save(selfReward);
                    log.info("Self reward granted: userId={}, rideId={}", ride.getUserId(), ride.getId());
                }

                // 2. Reward for referrer, if exists (idempotent)
                Long referrerId = userRepository.findReferrerIdByUserId(ride.getUserId());
                if (referrerId != null &&
                        !rewardRepo.existsByUserIdAndRideIdAndType(referrerId, ride.getId(), "referral")) {

                    BigDecimal referralPoints = BigDecimal.valueOf(ride.getDistanceKm() * 0.001);
                    RewardTransaction referralReward = new RewardTransaction(
                            referrerId,
                            ride.getId(),
                            referralPoints,
                            RewardTransaction.PointsType.earn,
                            "Referral reward",
                            "referral"
                    );
                    rewardRepo.save(referralReward);
                    log.info("Referral reward granted: referrerId={}, rideId={}", referrerId, ride.getId());
                }

                // 3. (Optional) Mark ride as rewarded (add a field to entity if needed)
                ride.setIsReward(true);
                rideRepository.save(ride);

            } catch (Exception e) {
                log.error("Error processing rideId={}: {}", ride.getId(), e.getMessage(), e);
            }
        }
    }

}