package com.easypan.entity.dto;

import lombok.Data;

@Data
public class DownloadFileDto {
    private String fileName;
    private String filePath;
    private String downloadCode;
}
