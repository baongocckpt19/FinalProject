package com.FinalProject.backend.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentGradeDto {
    private Integer studentId;
    private String fullName;
    private String username;

    private Double attendanceGrade;
    private Double midtermGrade;
    private Double finalGrade;

    private Double averageGrade; // 25% + 25% + 50%
}
