package com.maestronic.autoimportgtfs.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = Import.TABLE_NAME)
public class Import {

    public static final String TABLE_NAME= "import";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "task_name")
    private String taskName;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "detail")
    private String detail;

    @Column(name = "last_state")
    private String lastState;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "release_date")
    private LocalDateTime releaseDate;

    public Import() {
    }

    public Import(String taskName, String fileName, String fileType, String detail, String lastState, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.taskName = taskName;
        this.fileName = fileName;
        this.fileType = fileType;
        this.detail = detail;
        this.lastState = lastState;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getReleaseDate() {
        return releaseDate;
    }

    public String getStatus() {
        return status;
    }
}
