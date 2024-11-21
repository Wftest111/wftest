package com.sarthak.webapp.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;

@Configuration
public class LoggingConfig {
    private static final Logger logger = LoggerFactory.getLogger(LoggingConfig.class);

    @Value("${LOG_PATH:${user.home}/logs/webapp}")
    private String logPath;

    @PostConstruct
    public void init() {
        File logDirectory = new File(logPath);
        if (!logDirectory.exists()) {
            if (logDirectory.mkdirs()) {
                logger.info("Created log directory: {}", logPath);
            } else {
                logger.warn("Failed to create log directory: {}", logPath);
            }
        }
    }
}