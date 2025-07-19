package com.persona.mileage.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;

@Configuration
public class StartupDatabaseChecker {

    @Bean
    public CommandLineRunner checkDatabaseConnection(DataSource dataSource) {
        return args -> {
            System.out.print("Checking database connection... ");
            try (Connection connection = dataSource.getConnection()) {
                System.out.println("✅ Connected to MySQL successfully!");
            } catch (Exception e) {
                System.err.println("❌ Failed to connect to MySQL: " + e.getMessage());
            }
        };
    }
}