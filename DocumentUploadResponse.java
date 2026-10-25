package com.SpringAi.SpringAi2.Dto;

import java.time.LocalDateTime;

public class DocumentUploadResponse {

    private Long id;
    private String filename;
    private String fileType;
    private Long fileSize;
    private String message;
    private LocalDateTime uploadedAt;

    public DocumentUploadResponse() {
    }

    public DocumentUploadResponse(
            Long id,
            String filename,
            String fileType,
            Long fileSize,
            String message,
            LocalDateTime uploadedAt) {

        this.id = id;
        this.filename = filename;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.message = message;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public String getFileType() {
        return fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}