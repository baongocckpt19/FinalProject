package com.FinalProject.backend.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "Teacher")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TeacherId")
    private Integer teacherId;

    @Column(name = "AccountId", nullable = false)
    private Integer accountId;

    @Column(name = "FullName", nullable = false, length = 100)
    private String fullName;

    @Column(name = "Email", length = 100)
    private String email;

    // getters/setters ...
}
