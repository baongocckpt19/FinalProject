// src/main/java/com/FinalProject/backend/Dto/StudentScheduleDto.java
package com.FinalProject.backend.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentScheduleDto {
    private Integer scheduleId;
    private Integer classId;
    private String classCode;
    private String className;
    private LocalDate date;        // map với field "date" bên FE
    private LocalTime startTime;
    private LocalTime endTime;
    private String room;
    private Long studentCount;
    private Boolean isActive;
}
