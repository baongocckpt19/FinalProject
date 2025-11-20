package com.FinalProject.backend.Dto;

import lombok.Data;

@Data
public class GradeUpdateDto {
    private Integer studentId;
    private Double attendanceGrade;
    private Double midtermGrade;
    private Double finalGrade;
}
