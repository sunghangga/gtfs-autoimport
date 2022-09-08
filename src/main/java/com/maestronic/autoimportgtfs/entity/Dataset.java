package com.maestronic.autoimportgtfs.entity;

import java.time.LocalDateTime;

public class Dataset {
    private String fileName;
    private String downloadUrl;
    private LocalDateTime releaseDate;

    public Dataset() {
    }

    public Dataset(String fileName, String downloadUrl, LocalDateTime releaseDate) {
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
        this.releaseDate = releaseDate;
    }

    public Dataset(String fileName, String downloadUrl) {
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
