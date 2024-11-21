package com.sarthak.webapp.repository;

import com.sarthak.webapp.model.UserVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserVerificationRepository extends JpaRepository<UserVerification, String> {
    UserVerification findByUserIdAndVerified(Long userId, boolean verified);
}