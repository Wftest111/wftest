package com.sarthak.webapp.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String email;

    private String password;

    @Column(nullable = false)
    private boolean verified = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime accountCreated;

    @UpdateTimestamp
    private LocalDateTime accountUpdated;
}