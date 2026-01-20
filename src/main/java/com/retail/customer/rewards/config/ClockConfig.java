package com.retail.customer.rewards.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;
/**
 Provides a system Clock bean for injection into components that need to access "now".
 */
@Configuration
public class ClockConfig {
    @Bean
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }
}