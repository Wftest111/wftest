package com.sarthak.webapp.service;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarthak.webapp.model.User;
import com.sarthak.webapp.model.UserVerification;
import com.sarthak.webapp.repository.UserVerificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserVerificationService {
    private static final Logger logger = LoggerFactory.getLogger(UserVerificationService.class);

    private final AmazonSNS amazonSNS;
    private final UserVerificationRepository verificationRepository;
    private final ObjectMapper objectMapper;
    private final String snsTopicArn;
    private final int verificationExpiryMinutes;

    public UserVerificationService(
            AmazonSNS amazonSNS,
            UserVerificationRepository verificationRepository,
            ObjectMapper objectMapper,
            @Value("${aws.sns.topic.arn}") String snsTopicArn,
            @Value("${user.verification.expiry.minutes}") int verificationExpiryMinutes) {
        this.amazonSNS = amazonSNS;
        this.verificationRepository = verificationRepository;
        this.objectMapper = objectMapper;
        this.snsTopicArn = snsTopicArn;
        this.verificationExpiryMinutes = verificationExpiryMinutes;
    }

    public void sendVerificationEmail(User user) {
        try {
            // Create verification token
            String token = UUID.randomUUID().toString();
            LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(verificationExpiryMinutes);

            // Save verification record
            UserVerification verification = new UserVerification();
            verification.setToken(token);
            verification.setUser(user);
            verification.setVerified(false);
            verification.setExpiryTime(expiryTime);
            verification.setCreatedAt(LocalDateTime.now());
            verificationRepository.save(verification);

            // Prepare message for SNS
            Map<String, String> message = new HashMap<>();
            message.put("email", user.getEmail());
            message.put("firstName", user.getFirstName());
            message.put("verificationToken", token);

            // Publish to SNS
            PublishRequest publishRequest = new PublishRequest()
                    .withTopicArn(snsTopicArn)
                    .withMessage(objectMapper.writeValueAsString(message));
            amazonSNS.publish(publishRequest);

            logger.info("Verification email request sent for user: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send verification email for user: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    public boolean verifyUser(String token) {
        try {
            UserVerification verification = verificationRepository.findById(token)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

            if (verification.isVerified()) {
                throw new IllegalStateException("Email already verified");
            }

            if (LocalDateTime.now().isAfter(verification.getExpiryTime())) {
                throw new IllegalStateException("Verification link has expired");
            }

            User user = verification.getUser();
            user.setVerified(true);
            verification.setVerified(true);
            verificationRepository.save(verification);

            logger.info("User verified successfully: {}", user.getEmail());
            return true;
        } catch (Exception e) {
            logger.error("Verification failed for token: {}", token, e);
            return false;
        }
    }
}