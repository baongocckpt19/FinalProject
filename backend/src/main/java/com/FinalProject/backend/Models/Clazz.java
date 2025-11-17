package com.FinalProject.backend.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "Class")
@Data
public class Clazz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ClassId")
    private Integer classId;

    @Column(name = "ClassCode", nullable = false, unique = true)
    private String classCode;

    @Column(name = "ClassName", nullable = false)
    private String className;

    @Column(name = "TeacherId")
    private Integer teacherId;

    @Column(name = "CreatedDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "Status")
    private Boolean status; // 0: đang hoạt động, 1: đã hoàn thành

    @Column(name = "IsDeleted")
    private Boolean isDeleted; // 0 hiển thị, 1 ẩn
}
