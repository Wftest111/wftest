package com.sarthak.webapp.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_images")
@Data
public class UserImage {
    @Id
    private String id;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "url")
    private String url;

    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "size")
    private Long size;
}