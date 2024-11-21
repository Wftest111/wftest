package com.sarthak.webapp.repository;

import com.sarthak.webapp.model.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserImageRepository extends JpaRepository<UserImage, String> {
    Optional<UserImage> findByUserId(Long userId);
}