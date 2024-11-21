package com.sarthak.webapp.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class MetricsShutdownConfig {

    @Autowired(required = false)
    private MeterRegistry meterRegistry;

    @PreDestroy
    public void cleanupMetrics() {
        if (meterRegistry != null) {
            meterRegistry.close();
        }
    }
}