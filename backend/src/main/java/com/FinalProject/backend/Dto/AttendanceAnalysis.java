package com.FinalProject.backend.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class AttendanceAnalysis {

    @JsonProperty("overallStatistics")
    public Stats stats;

    @JsonProperty("highRiskStudents")
    public List<RiskStudent> riskStudents;

    public List<WeeklyTrend> weeklyTrend;
    public List<PerStudent> perStudent;
    public List<String> recommendations;

    public static class Stats {
        public int totalRecords;
        public int presentCount;
        public int absentCount;
        public int lateCount;
        public int onLeaveCount;
        public double attendanceRate;
    }

    public static class RiskStudent {
        public String studentCode;
        public String fullName;
        public int absentCount;
        public int lateCount;
        public double riskScore;
        public String riskReason;
    }

    public static class WeeklyTrend {
        public String weekStart;
        public String weekEnd;
        public int present;
        public int absent;
        public int late;
    }

    public static class PerStudent {
        public String studentCode;
        public String fullName;
        public int presentCount;  // đổi từ present
        public int absentCount;   // đổi từ absent
        public int lateCount;     // đổi từ late
        public int onLeaveCount;  // đổi từ onLeave
        public int totalSessions; // thêm mới
        public double attendanceRate;
    }
}
