package com.sarthak.webapp.config;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class SNSConfig {

    @Value("${aws.region}")
    private String awsRegion;

    @Bean
    public AmazonSNS amazonSNS() {
        return AmazonSNSClientBuilder.standard()
                .withRegion(awsRegion)
                .build();
    }
}