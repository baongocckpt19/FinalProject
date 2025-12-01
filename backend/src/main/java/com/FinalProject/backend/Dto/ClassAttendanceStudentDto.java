// src/main/java/com/FinalProject/backend/Dto/ClassAttendanceStudentDto.java
package com.FinalProject.backend.Dto;

import lombok.Data;

@Data
public class ClassAttendanceStudentDto {
    private Integer classId;
    private Integer scheduleId;

    private Integer studentId;
    private String fullName;
    private String  studentCode;
    private String username;

    private String email;
    private String phone;


    private Integer attendanceId;   // có thể null nếu chưa có bản ghi
    private String status;          // "present" / "absent" / "late" / "none"
    private String attendanceTime;  // "HH:mm:ss" hoặc null

    // ===== Thêm 5 trường thống kê để khớp FE =====
    private Integer totalSessions;     // tổng số buổi của lớp
    private Integer presentSessions;   // số buổi có mặt
    private Integer lateSessions;      // số buổi đi muộn
    private Integer absentSessions;    // số buổi vắng
    private Integer attendanceRate;    // % (0-100)
}
