package com.sarthak.webapp.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sarthak.webapp.model.ImageResponseDTO;
import com.sarthak.webapp.model.User;
import com.sarthak.webapp.model.UserImage;
import com.sarthak.webapp.repository.UserImageRepository;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;
import java.io.IOException;

//@Service
//public class ImageService {
//    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);
//
//    private final UserImageRepository imageRepository;
//    private final AmazonS3 amazonS3;
//    private final String bucketName;
//
//    public ImageService(
//            UserImageRepository imageRepository,
//            AmazonS3 amazonS3,
//            @Value("${aws.s3.bucket}") String bucketName) {
//        this.imageRepository = imageRepository;
//        this.amazonS3 = amazonS3;
//        this.bucketName = bucketName;
//    }
//
//    @Timed(value = "s3.get.image")
//    public ImageResponseDTO getImage(Long userId) {
//        UserImage image = imageRepository.findByUserId(userId)
//                .orElseThrow(() -> new IllegalArgumentException("Image not found"));
//        return mapToResponseDto(image);
//    }
//
//    @Timed(value = "s3.upload.time")
//    public ImageResponseDTO uploadImage(MultipartFile file, User user) throws IOException {
//        if (file.isEmpty()) {
//            throw new IllegalArgumentException("File cannot be empty");
//        }
//
//        String contentType = file.getContentType();
//        if (contentType == null || !(contentType.equals("image/jpeg") ||
//                contentType.equals("image/jpg") ||
//                contentType.equals("image/png"))) {
//            throw new IllegalArgumentException("Invalid file type");
//        }
//
//        // Delete existing image if present
//        imageRepository.findByUserId(user.getId())
//                .ifPresent(this::deleteImageFromS3AndDB);
//
//        // Generate unique filename
//        String fileName = UUID.randomUUID().toString() + getFileExtension(file.getOriginalFilename());
//        String s3Key = String.format("users/%d/%s", user.getId(), fileName);
//
//        // Upload to S3
//        ObjectMetadata metadata = new ObjectMetadata();
//        metadata.setContentType(contentType);
//        metadata.setContentLength(file.getSize());
//
//        amazonS3.putObject(new PutObjectRequest(bucketName, s3Key, file.getInputStream(), metadata));
//
//        // Save to database
//        UserImage image = new UserImage();
//        image.setId(UUID.randomUUID().toString());
//        image.setFileName(fileName);
//        image.setUrl(s3Key);
//        image.setUploadDate(LocalDateTime.now());
//        image.setUser(user);
//        image.setContentType(contentType);
//        image.setSize(file.getSize());
//
//        UserImage savedImage = imageRepository.save(image);
//        return mapToResponseDto(savedImage);
//    }
//
//    @Timed(value = "s3.delete.time")
//    public void deleteImage(Long userId) {
//        UserImage image = imageRepository.findByUserId(userId)
//                .orElseThrow(() -> new IllegalArgumentException("Image not found"));
//
//        try {
//            // Delete from S3
//            amazonS3.deleteObject(bucketName, image.getUrl());
//            // Delete from database
//            imageRepository.delete(image);
//
//            logger.info("Successfully deleted image for user {}", userId);
//        } catch (Exception e) {
//            logger.error("Error deleting image for user {}: {}", userId, e.getMessage());
//            throw new RuntimeException("Failed to delete image", e);
//        }
//    }
//
//    private ImageResponseDTO mapToResponseDto(UserImage image) {
//        ImageResponseDTO dto = new ImageResponseDTO();
//        dto.setFileName(image.getFileName());
//        dto.setId(image.getId());
//        dto.setUrl(String.format("%s/%s", bucketName, image.getUrl()));
//        dto.setUploadDate(image.getUploadDate().format(DateTimeFormatter.ISO_DATE));
//        dto.setUserId(image.getUser().getId().toString());
//        return dto;
//    }
//
//    private String getFileExtension(String fileName) {
//        return fileName.substring(fileName.lastIndexOf("."));
//    }
//
//    private void deleteImageFromS3AndDB(UserImage image) {
//        try {
//            amazonS3.deleteObject(bucketName, image.getUrl());
//            imageRepository.delete(image);
//        } catch (Exception e) {
//            logger.error("Error deleting existing image: {}", e.getMessage());
//            throw new RuntimeException("Failed to delete existing image", e);
//        }
//    }
//}
@Service
public class ImageService {
    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    private final UserImageRepository imageRepository;
    private final AmazonS3 amazonS3;
    private final String bucketName;

    public ImageService(
            UserImageRepository imageRepository,
            AmazonS3 amazonS3,
            @Value("${aws.s3.bucket}") String bucketName) {
        this.imageRepository = imageRepository;
        this.amazonS3 = amazonS3;
        this.bucketName = bucketName;
        logger.info("ImageService initialized with bucket: {}", bucketName);
    }

    @Timed(value = "s3.get.image")
    public ImageResponseDTO getImage(Long userId) {
        logger.info("Fetching image for user ID: {}", userId);
        try {
            UserImage image = imageRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Image not found"));
            logger.info("Successfully retrieved image for user ID: {}", userId);
            return mapToResponseDto(image);
        } catch (Exception e) {
            logger.error("Failed to retrieve image for user ID: {}. Error: {}", userId, e.getMessage());
            throw e;
        }
    }

    @Timed(value = "s3.upload.time")
    public ImageResponseDTO uploadImage(MultipartFile file, User user) throws IOException {
        logger.info("Starting image upload for user ID: {}", user.getId());

        // Validate file is not empty
        if (file.isEmpty()) {
            logger.error("Empty file received from user ID: {}", user.getId());
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Validate content type
        String contentType = file.getContentType();
        logger.debug("Received file with content type: {}", contentType);
        if (contentType == null || !(contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png"))) {
            logger.error("Invalid file type received from user ID: {}: {}", user.getId(), contentType);
            throw new IllegalArgumentException("Invalid file type. Only JPEG, JPG, and PNG are allowed");
        }

        // Check for existing image
        Optional<UserImage> existingImage = imageRepository.findByUserId(user.getId());
        if (existingImage.isPresent()) {
            // Compare file content/size to check if it's the same image
            UserImage currentImage = existingImage.get();
            if (currentImage.getSize() == file.getSize() &&
                    currentImage.getContentType().equals(contentType)) {
                logger.warn("User ID: {} attempting to upload potentially duplicate image", user.getId());
                throw new IllegalArgumentException("This image appears to be already uploaded");
            }

            logger.info("Deleting existing image for user ID: {}", user.getId());
            deleteImageFromS3AndDB(currentImage);
        }

        try {
            // Generate unique filename
            String fileName = UUID.randomUUID().toString() + getFileExtension(file.getOriginalFilename());
            String s3Key = String.format("users/%d/%s", user.getId(), fileName);
            logger.info("Generated S3 key for user ID: {}: {}", user.getId(), s3Key);

            // Upload to S3
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(file.getSize());

            amazonS3.putObject(new PutObjectRequest(bucketName, s3Key, file.getInputStream(), metadata));
            logger.info("Successfully uploaded file to S3 for user ID: {}", user.getId());

            // Save to database
            UserImage image = new UserImage();
            image.setId(UUID.randomUUID().toString());
            image.setFileName(fileName);
            image.setUrl(s3Key);
            image.setUploadDate(LocalDateTime.now());
            image.setUser(user);
            image.setContentType(contentType);
            image.setSize(file.getSize());

            UserImage savedImage = imageRepository.save(image);
            logger.info("Successfully saved image metadata to database for user ID: {}", user.getId());

            return mapToResponseDto(savedImage);
        } catch (Exception e) {
            logger.error("Failed to upload image for user ID: {}. Error: {}", user.getId(), e.getMessage());
            throw new RuntimeException("Failed to upload image: " + e.getMessage());
        }
    }

    @Timed(value = "s3.delete.time")
    public void deleteImage(Long userId) {
        logger.info("Attempting to delete image for user ID: {}", userId);
        try {
            UserImage image = imageRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Image not found"));

            deleteImageFromS3AndDB(image);
            logger.info("Successfully deleted image for user ID: {}", userId);
        } catch (IllegalArgumentException e) {
            logger.warn("No image found to delete for user ID: {}", userId);
            throw e;
        } catch (Exception e) {
            logger.error("Failed to delete image for user ID: {}. Error: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to delete image: " + e.getMessage());
        }
    }

    private ImageResponseDTO mapToResponseDto(UserImage image) {
        ImageResponseDTO dto = new ImageResponseDTO();
        dto.setFileName(image.getFileName());
        dto.setId(image.getId());
        dto.setUrl(String.format("%s/%s", bucketName, image.getUrl()));
        dto.setUploadDate(image.getUploadDate().format(DateTimeFormatter.ISO_DATE));
        dto.setUserId(image.getUser().getId().toString());
        return dto;
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private void deleteImageFromS3AndDB(UserImage image) {
        try {
            logger.debug("Deleting image from S3: {}", image.getUrl());
            amazonS3.deleteObject(bucketName, image.getUrl());

            logger.debug("Deleting image from database: {}", image.getId());
            imageRepository.delete(image);

            logger.info("Successfully deleted image: {}", image.getId());
        } catch (Exception e) {
            logger.error("Failed to delete image: {}. Error: {}", image.getId(), e.getMessage());
            throw new RuntimeException("Failed to delete existing image: " + e.getMessage());
        }
    }
}