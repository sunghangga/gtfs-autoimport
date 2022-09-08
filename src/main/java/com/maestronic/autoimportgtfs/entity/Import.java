package com.maestronic.autoimportgtfs.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = Import.TABLE_NAME)
@AllArgsConstructor
@NoArgsConstructor
@Data
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

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "release_date")
    private LocalDateTime releaseDate;

    public LocalDateTime getReleaseDate() {
        return releaseDate;
    }

    public String getStatus() {
        return status;
    }
}
