package com.maestronic.autoimportgtfs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {

    private LocalDateTime jobRunTime;
    private Long fileSize;
    private String status;
    private Object files;
    private Object requestDetails;
    private Object responseDetails;
}
