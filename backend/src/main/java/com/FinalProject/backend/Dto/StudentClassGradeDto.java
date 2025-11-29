// src/main/java/com/FinalProject/backend/Dto/StudentClassGradeDto.java
package com.FinalProject.backend.Dto;

import lombok.Data;

@Data
public class StudentClassGradeDto {

    private Integer classId;
    private String classCode;
    private String className;
    private Boolean status;      // 0 = đang hoạt động, 1 = đã hoàn thành
    private String teacherName;

    private Double attendanceGrade;
    private Double midtermGrade;
    private Double finalGrade;

    private Double averageGrade; // null nếu chưa đủ 3 đầu điểm
}
