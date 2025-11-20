package com.FinalProject.backend.Models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Grade")
@Data
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GradeId")
    private Integer gradeId;

    @Column(name = "StudentId", nullable = false)
    private Integer studentId;

    @Column(name = "ClassId", nullable = false)
    private Integer classId;

    @Column(name = "AttendanceGrade")
    private Double attendanceGrade;

    @Column(name = "MidtermGrade")
    private Double midtermGrade;

    @Column(name = "FinalGrade")
    private Double finalGrade;
}
