package com.solvia.assistantrh.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Horloge unique de l'application. Les services l'injectent au lieu d'appeler Instant.now(),
 * ce qui permet de la figer dans les tests.
 */
@Configuration
public class ClockConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
