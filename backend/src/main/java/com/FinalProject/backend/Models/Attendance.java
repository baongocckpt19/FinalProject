// src/main/java/com/FinalProject/backend/Models/Attendance.java
package com.FinalProject.backend.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;

@Data
@Entity
@Table(
        name = "Attendance",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"StudentId", "ScheduleId"})
        }
)
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AttendanceId")
    private Integer attendanceId;

    @Column(name = "StudentId", nullable = false)
    private Integer studentId;

    @Column(name = "ClassId", nullable = false)
    private Integer classId;

    @Column(name = "ScheduleId", nullable = false)
    private Integer scheduleId;

    @Column(name = "AttendanceTime", nullable = false)
    private LocalTime attendanceTime;   // giờ chấm vân tay

    @Column(name = "Status", nullable = false, length = 50)
    private String status;              // "Có mặt" / "Vắng" / "Muộn"
}
