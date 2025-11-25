package com.FinalProject.backend.Dto;

import lombok.Data;

@Data
public class AttendanceStudentDto {
    private Integer attendanceId;
    private Integer studentId;
    private String fullName;
    private String username;

    private String status;          // "present" / "absent" / "late" (map từ "Có mặt"/"Vắng"/"Muộn")
    private String attendanceTime;  // "HH:mm:ss" (có thể null)

    private Integer totalSessions;      // tổng số buổi của lớp
    private Integer presentSessions;    // số buổi có mặt
    private Integer lateSessions;       // số buổi muộn
    private Integer absentSessions;     // số buổi vắng
    private Double attendanceRate;      // % (present+late)/totalSessions*100
}
