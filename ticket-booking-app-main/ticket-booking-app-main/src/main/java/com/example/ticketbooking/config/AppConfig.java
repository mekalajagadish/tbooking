package com.example.ticketbooking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AppConfig {
    /** Injected wherever "now" matters, so time-based rules can be tested with a fixed clock. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
