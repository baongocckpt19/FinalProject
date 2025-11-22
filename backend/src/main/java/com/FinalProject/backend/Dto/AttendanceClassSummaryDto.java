// src/main/java/com/FinalProject/backend/Dto/AttendanceClassSummaryDto.java
package com.FinalProject.backend.Dto;

import lombok.Data;

@Data
public class AttendanceClassSummaryDto {
    private Integer classId;
    private String classCode;
    private String className;
    private String time;      // "HH:mm - HH:mm"
    private String status;    // "Đang hoạt động" / "Đã kết thúc"
    private Integer total;
    private Integer present;
    private Integer absent;
    private Integer late;
    private Double rate;      // % điểm danh
}
