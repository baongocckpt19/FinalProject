package com.FinalProject.backend.Dto;

import lombok.Data;

@Data
public class StudentAttendanceHistoryDto {
    private Integer attendanceId;
    private String date;          // yyyy-MM-dd
    private String sessionTime;   // "HH:mm - HH:mm"
    private String status;        // "present" / "absent" / "late"
    private String attendanceTime; // "HH:mm:ss" or null
}
