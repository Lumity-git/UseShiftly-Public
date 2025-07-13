package com.hotel.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class HotelSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelSchedulerApplication.class, args);
    }
}
