// src/main/java/com/FinalProject/backend/Dto/AttendanceCalendarDayDto.java
package com.FinalProject.backend.Dto;

import lombok.Data;

@Data
public class AttendanceCalendarDayDto {
    private String date;      // yyyy-MM-dd
    private Integer classCount;
}
