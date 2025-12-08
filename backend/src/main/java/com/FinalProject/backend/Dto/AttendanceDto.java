package com.FinalProject.backend.Dto;

import lombok.Data;

@Data
public class AttendanceDto {
    private String studentCode;
    private String fullName;
    private String scheduleDate;
    private String startTime;
    private String endTime;
    private String status;
}
