package com.persona.mileage.job;

import com.persona.mileage.service.MileageService;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@Component
public class MileageJobRunner {
    private static final Logger log = LoggerFactory.getLogger(MileageJobRunner.class);

    @Autowired
    private MileageService mileageService;

    @Scheduled(cron = "0 0 2 * * *")
    public void run() {
        String result = mileageService.executeMileageJob();
        mileageService.sendResultEmail(result);
    }

    @PreDestroy
    public void init() {
        log.info("MileageJobRunner job started at {}", LocalDateTime.now());
    }
}
