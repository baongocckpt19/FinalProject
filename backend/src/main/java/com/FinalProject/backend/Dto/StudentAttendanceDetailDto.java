package com.FinalProject.backend.Dto;

import lombok.Data;

import java.util.List;

@Data
public class StudentAttendanceDetailDto {
    private Integer studentId;
    private String fullName;
    private String username;

    private String email;
    private String phone;

    private Integer classId;
    private String classCode;
    private String className;

    private Integer totalSessions;    // tổng buổi của lớp
    private Integer presentSessions;  // số buổi có mặt
    private Integer lateSessions;     // số buổi muộn
    private Integer absentSessions;   // số buổi vắng
    private Double attendanceRate;    // % tham gia (present+late)/totalSessions*100

    private List<StudentAttendanceHistoryDto> history;
}
