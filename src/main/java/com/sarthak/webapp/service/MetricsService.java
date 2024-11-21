package com.sarthak.webapp.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class MetricsService {
    private final Counter userCreationCounter;
    private final Counter imageUploadCounter;
    private final Timer dbOperationTimer;
    private final Timer s3OperationTimer;

    public MetricsService(MeterRegistry registry) {
        // Counters for operations
        this.userCreationCounter = Counter.builder("webapp.user.creation")
                .description("Number of users created")
                .register(registry);

        this.imageUploadCounter = Counter.builder("webapp.image.uploads")
                .description("Number of images uploaded")
                .register(registry);

        // Timers for performance monitoring
        this.dbOperationTimer = Timer.builder("webapp.db.operation.time")
                .description("Time taken for database operations")
                .register(registry);

        this.s3OperationTimer = Timer.builder("webapp.s3.operation.time")
                .description("Time taken for S3 operations")
                .register(registry);
    }

    // Methods to increment counters
    public void incrementUserCreations() {
        userCreationCounter.increment();
    }

    public void incrementImageUploads() {
        imageUploadCounter.increment();
    }

    // Methods to record operation times
    public void recordDbOperationTime(long timeInMs) {
        dbOperationTimer.record(timeInMs, TimeUnit.MILLISECONDS);
    }

    public void recordS3OperationTime(long timeInMs) {
        s3OperationTimer.record(timeInMs, TimeUnit.MILLISECONDS);
    }
}