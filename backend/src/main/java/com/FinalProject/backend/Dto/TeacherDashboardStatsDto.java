package com.FinalProject.backend.Dto;

import lombok.Data;

@Data
public class TeacherDashboardStatsDto {
    private int totalStudents;     // Tổng số sinh viên
    private int activeClasses;     // Số lớp đang dạy & đang hoạt động
    private double averageScore;   // Điểm trung bình
    private double attendanceRate; // Tỷ lệ điểm danh (0-100)
}
