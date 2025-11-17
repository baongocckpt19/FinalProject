package com.FinalProject.backend.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "Student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "StudentId")
    private Integer studentId;

    @Column(name = "AccountId", nullable = false)
    private Integer accountId;

    @Column(name = "FullName", nullable = false, length = 100)
    private String fullName;

    @Column(name = "Email", length = 100)
    private String email;

    // getters/setters ...
}
