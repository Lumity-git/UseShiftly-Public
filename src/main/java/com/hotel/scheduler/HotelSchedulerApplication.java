package com.hotel.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Hotel Employee Scheduler Spring Boot application.
 * <p>
 * Enables async processing and scheduled tasks for shift notifications and background jobs.
 * <b>Usage:</b> Run as a standalone Java application to start the backend server.
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class HotelSchedulerApplication {

    /**
     * Starts the Hotel Employee Scheduler Spring Boot application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(HotelSchedulerApplication.class, args);
    }
}
