package com.sarthak.webapp.controller;

import com.sarthak.webapp.service.UserVerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
public class EmailVerificationController {

    private final UserVerificationService verificationService;

    public EmailVerificationController(UserVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @GetMapping("/verifyEmail")
    public ResponseEntity<String> verifyEmail(
            @RequestParam("token") String token) {
        boolean verified = verificationService.verifyUser(token);
        if (verified) {
            return ResponseEntity.ok("Email verified successfully");
        }
        return ResponseEntity.badRequest().body("Verification failed");
    }
}