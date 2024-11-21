package com.sarthak.webapp.controller;

import com.sarthak.webapp.model.ImageResponseDTO;
import com.sarthak.webapp.model.User;
import com.sarthak.webapp.service.ImageService;
import com.sarthak.webapp.service.UserService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;

@RestController
@RequestMapping("/v1/user/self/pic")
public class ImageController {
    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);
    private final ImageService imageService;
    private final UserService userService;
    private final MeterRegistry meterRegistry;

    public ImageController(ImageService imageService, UserService userService, MeterRegistry meterRegistry) {
        this.imageService = imageService;
        this.userService = userService;
        this.meterRegistry = meterRegistry;
    }

    @GetMapping
    @Timed(value = "api.get.image", description = "Time taken to retrieve image")
    public ResponseEntity<ImageResponseDTO> getImage(Principal principal) {
        long startTime = System.currentTimeMillis();
        logger.info("Getting image for user: {}", principal.getName());
        try {
            User user = userService.getUserEntityByEmail(principal.getName());

            if (!user.isVerified()) {
                logger.warn("Unverified user attempting to get image: {}", principal.getName());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(null);
            }

            ImageResponseDTO response = imageService.getImage(user.getId());

            meterRegistry.counter("image.get.success").increment();
            meterRegistry.timer("image.get.time")
                    .record(System.currentTimeMillis() - startTime, java.util.concurrent.TimeUnit.MILLISECONDS);

            logger.info("Successfully retrieved image for user: {}", principal.getName());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Image not found for user: {}", principal.getName());
            meterRegistry.counter("image.get.notfound").increment();
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Failed to get image for user: {}. Error: {}", principal.getName(), e.getMessage());
            meterRegistry.counter("image.get.error",
                    "error", e.getClass().getSimpleName()).increment();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @Timed(value = "api.upload.image", description = "Time taken to upload image")
    public ResponseEntity<ImageResponseDTO> uploadImage(
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        long startTime = System.currentTimeMillis();
        logger.info("Received image upload request from user: {}. File size: {} bytes",
                principal.getName(), file.getSize());

        try {
            User user = userService.getUserEntityByEmail(principal.getName());

            if (!user.isVerified()) {
                logger.warn("Unverified user attempting to upload image: {}", principal.getName());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(null);
            }

            meterRegistry.gauge("image.upload.size", file.getSize());

            ImageResponseDTO response = imageService.uploadImage(file, user);

            meterRegistry.counter("image.upload.success").increment();
            meterRegistry.timer("image.upload.time")
                    .record(System.currentTimeMillis() - startTime, java.util.concurrent.TimeUnit.MILLISECONDS);

            logger.info("Successfully uploaded image for user: {}. Image ID: {}",
                    principal.getName(), response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid image upload request from user: {}. Error: {}",
                    principal.getName(), e.getMessage());
            meterRegistry.counter("image.upload.invalid").increment();
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Failed to upload image for user: {}. Error: {}",
                    principal.getName(), e.getMessage(), e);
            meterRegistry.counter("image.upload.error",
                    "error", e.getClass().getSimpleName()).increment();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping
    @Timed(value = "api.delete.image", description = "Time taken to delete image")
    public ResponseEntity<Void> deleteImage(Principal principal) {
        long startTime = System.currentTimeMillis();
        logger.info("Deleting image for user: {}", principal.getName());
        try {
            User user = userService.getUserEntityByEmail(principal.getName());

            if (!user.isVerified()) {
                logger.warn("Unverified user attempting to delete image: {}", principal.getName());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .build();
            }

            imageService.deleteImage(user.getId());

            meterRegistry.counter("image.delete.success").increment();
            meterRegistry.timer("image.delete.time")
                    .record(System.currentTimeMillis() - startTime, java.util.concurrent.TimeUnit.MILLISECONDS);

            logger.info("Successfully deleted image for user: {}", principal.getName());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Failed to delete image for user: {}. Error: {}",
                    principal.getName(), e.getMessage());
            meterRegistry.counter("image.delete.error",
                    "error", e.getClass().getSimpleName()).increment();
            return ResponseEntity.internalServerError().build();
        }
    }
}