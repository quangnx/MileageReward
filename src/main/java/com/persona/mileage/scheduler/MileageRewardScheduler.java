package com.persona.mileage.scheduler;

import com.persona.mileage.service.MileageRewardService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class MileageRewardScheduler {
    private static final Logger logger = LoggerFactory.getLogger(MileageRewardScheduler.class);

    private final MileageRewardService mileageRewardService;

    @Value("${job.mileage.reward.enabled}")
    private boolean jobEnabled;

    public MileageRewardScheduler(MileageRewardService mileageRewardService) {
        this.mileageRewardService = mileageRewardService;
    }

    @Scheduled(cron = "${job.mileage.reward.cron}")
    public void runDailyRewardJob() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        logger.info("▶️ MileageRewardScheduler triggered at {}", time);
        try
        {
            if (!jobEnabled) {
                System.out.println("Report job MileageRewardScheduler is disabled.");
                return;
            }
            mileageRewardService.processDailyRewards();
            logger.info("✅ MileageRewardScheduler completed at {}", time);
        } catch (Exception e) {
            logger.error("❌ MileageRewardScheduler failed at {}", time, e);
        }

    }

    public boolean isJobEnabled() {
        return jobEnabled;
    }
}