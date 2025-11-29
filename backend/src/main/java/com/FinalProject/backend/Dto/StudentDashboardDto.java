// StudentDashboardDto.java
package com.FinalProject.backend.Dto;
import lombok.Data;
import java.util.List;
@Data
public class StudentDashboardDto {

    private Double gpa;             // GPA
    private Double passRate;        // % qua môn (0-100)
    private Integer absentCount;    // số buổi vắng
    private Double attendanceRate;  // % điểm danh (0-100)

    private List<GradeByClassDto> grades;          // max 6 lớp
    private AttendanceSummaryDto attendanceSummary; // cho biểu đồ tròn

    // getters + setters
}
