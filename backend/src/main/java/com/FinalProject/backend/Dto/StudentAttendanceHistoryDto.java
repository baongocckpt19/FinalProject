// src/main/java/com/FinalProject/backend/Dto/StudentAttendanceHistoryDto.java
package com.FinalProject.backend.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentAttendanceHistoryDto {
    private Integer scheduleId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    private String status;          // "present" / "absent" / "late" / "none"
    private String attendanceTime;  // "HH:mm:ss" hoặc null
}
