package com.sarthak.webapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ImageResponseDTO {
    @JsonProperty("file_name")
    private String fileName;

    private String id;

    private String url;

    @JsonProperty("upload_date")
    private String uploadDate;

    @JsonProperty("user_id")
    private String userId;
}