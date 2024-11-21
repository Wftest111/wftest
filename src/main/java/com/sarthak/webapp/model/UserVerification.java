package com.sarthak.webapp.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_verifications")
@Data
public class UserVerification {
    @Id
    private String token;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private boolean verified;

    @Column(nullable = false)
    private LocalDateTime expiryTime;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}