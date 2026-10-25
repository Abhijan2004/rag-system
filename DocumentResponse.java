package com.SpringAi.SpringAi2.Dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentResponse {
    private Long id;
    private String filename;
    private String fileType;
    private Long fileSize;
    private String summary;
    private LocalDateTime uploadedAt;
}
