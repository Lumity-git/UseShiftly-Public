package com.useshiftly.scheduler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Configuration class for providing a centralized Clock bean.
 * <p>
 * Ensures all time-based operations use the America/Chicago timezone consistently
 * across the application. This facilitates testing and maintains timezone consistency.
 * <p>
 * <b>Usage:</b> Inject Clock into services instead of using OffsetDateTime.now() directly.
 */
@Configuration
public class ClockConfig {

    /**
     * Provides a Clock bean configured for America/Chicago timezone.
     * 
     * @return Clock instance with America/Chicago zone
     */
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Chicago"));
    }
}
