package com.FinalProject.backend.Dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ClassScheduleDto {

    private Integer scheduleId;
    private Integer classId;

    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private String room;
    private Boolean isActive;
}
