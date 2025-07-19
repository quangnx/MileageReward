package com.persona.mileage.service;

import com.persona.mileage.job.MileageJobRunner;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;

@Service
public class MileageService {
    private static final Logger log = LoggerFactory.getLogger(MileageJobRunner.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EmailService emailService;

    @PostConstruct
    public void init() {
        log.info("Mileage job started at {}", LocalDateTime.now());
        // sendEmail("Mileage job started at " + LocalDateTime.now());
    }
    public String executeMileageJob() {
        return jdbcTemplate.execute((ConnectionCallback<String>) conn -> {
            try (CallableStatement cs = conn.prepareCall("{call Job_Mileage_Midnight()}")) {
                boolean hasResult = cs.execute();
                StringBuilder sb = new StringBuilder("Kết quả thủ tục:\n");
                if (hasResult) {
                    ResultSet rs = cs.getResultSet();
                    while (rs.next()) {
                        sb.append(rs.getString(1)).append("\n");
                    }
                }
                return sb.toString();
            }
        });
    }

    public void sendResultEmail(String content) {
        emailService.sendEmail("recipient@example.com", "Mileage Job Result", content);
    }
}
