package com.maestronic.autoimportgtfs.dto;

import java.time.LocalDateTime;

public class DatasetDto {
    private String fileName;
    private String downloadUrl;
    private LocalDateTime releaseDate;

    public DatasetDto() {
    }

    public DatasetDto(String fileName, String downloadUrl, LocalDateTime releaseDate) {
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
        this.releaseDate = releaseDate;
    }

    public DatasetDto(String fileName, String downloadUrl) {
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public LocalDateTime getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDateTime releaseDate) {
        this.releaseDate = releaseDate;
    }
}
