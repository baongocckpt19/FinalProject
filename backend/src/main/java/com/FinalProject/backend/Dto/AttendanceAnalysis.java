//đây là AttendanceAnalysis.java
package com.FinalProject.backend.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data // 🟢 Thêm Data cho class cha
public class AttendanceAnalysis {

    @JsonProperty("overallStatistics")
    private Stats stats; // Nên để private

    @JsonProperty("highRiskStudents")
    private List<RiskStudent> riskStudents;

    @JsonProperty("weeklyTrend") // Map chính xác tên field JSON
    private List<WeeklyTrend> weeklyTrend;

    @JsonProperty("perStudent")
    private List<PerStudent> perStudent;

    @JsonProperty("recommendations")
    private List<String> recommendations;

    @Data // 🟢 Thêm Data cho các class con
    public static class Stats {
        private int totalRecords;
        private int presentCount;
        private int absentCount;
        private int lateCount;
        private int onLeaveCount;
        private double attendanceRate;
    }

    @Data
    public static class RiskStudent {
        private String studentCode;
        private String fullName;
        private int absentCount;
        private int lateCount;
        private double riskScore;
        private String riskReason;
    }

    @Data
    public static class WeeklyTrend {
        private String weekStart;
        private String weekEnd;
        private int present;
        private int absent;
        private int late;
    }

    @Data
    public static class PerStudent {
        private String studentCode;
        private String fullName;
        private int presentCount;
        private int absentCount;
        private int lateCount;
        private int onLeaveCount;
        private int totalSessions;
        private double attendanceRate;
    }
}