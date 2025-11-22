// src/main/java/com/FinalProject/backend/Models/Attendance.java
package com.FinalProject.backend.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "Attendance")
@Data
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AttendanceId")
    private Integer attendanceId;

    @Column(name = "StudentId", nullable = false)
    private Integer studentId;

    @Column(name = "ClassId", nullable = false)
    private Integer classId;

    @Column(name = "AttendanceDate", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "AttendanceTime", nullable = false)
    private LocalTime attendanceTime;

    @Column(name = "SessionStart", nullable = false)
    private LocalTime sessionStart;

    @Column(name = "SessionEnd", nullable = false)
    private LocalTime sessionEnd;

    @Column(name = "Status", nullable = false, length = 50)
    private String status; // Có mặt / Vắng / Muộn ...
}
