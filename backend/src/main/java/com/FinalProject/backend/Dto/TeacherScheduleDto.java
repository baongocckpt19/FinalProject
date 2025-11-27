// TeacherScheduleDto.java
package com.FinalProject.backend.Dto;


import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherScheduleDto {
    private Integer scheduleId;
    private Integer classId;
    private String classCode;
    private String className;
    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String room;
    private Long studentCount;
    private Boolean isActive;

    // getter/setter
}
