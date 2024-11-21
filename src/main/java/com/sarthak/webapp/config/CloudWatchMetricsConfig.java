package com.sarthak.webapp.config;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

import java.time.Duration;
import java.util.Map;

@Configuration
public class CloudWatchMetricsConfig {

    @Value("${management.metrics.export.cloudwatch.namespace}")
    private String namespace;

    @Value("${management.metrics.export.cloudwatch.step}")
    private String step;

    @Bean
    public CloudWatchAsyncClient cloudWatchAsyncClient() {
        return CloudWatchAsyncClient.builder().build();
    }

    @Bean
    public MeterRegistry getMeterRegistry() {
        CloudWatchConfig cloudWatchConfig = new CloudWatchConfig() {
            private final Map<String, String> configuration = Map.of(
                    "cloudwatch.namespace", namespace,
                    "cloudwatch.step", step
            );

            @Override
            public String get(String key) {
                return configuration.get(key);
            }
        };

        MeterRegistry registry = new CloudWatchMeterRegistry(
                cloudWatchConfig,
                Clock.SYSTEM,
                cloudWatchAsyncClient()
        );

        // Configure metrics filters and common tags
        registry.config()
                .meterFilter(MeterFilter.ignoreTags("path")) // Optional: if you want to reduce cardinality
                .commonTags("application", "webapp")
                .meterFilter(MeterFilter.accept(id -> {
                    String name = id.getName();
                    // Define which metrics you want to export
                    return name.startsWith("http.") ||
                            name.startsWith("image.") ||
                            name.startsWith("webapp.") ||
                            name.startsWith("db.");
                }));

        return registry;
    }
}