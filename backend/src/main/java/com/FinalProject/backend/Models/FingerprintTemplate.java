// FILE: src/main/java/com/FinalProject/backend/Models/FingerprintTemplate.java
package com.FinalProject.backend.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "FingerprintTemplate")
public class FingerprintTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TemplateId")
    private Integer templateId;

    @Column(name = "StudentId", nullable = false)
    private Integer studentId;

    @Lob
    @Column(name = "TemplateData", nullable = false)
    private byte[] templateData;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // getters & setters
    public Integer getTemplateId() {
        return templateId;
    }
    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }
    public Integer getStudentId() {
        return studentId;
    }
    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }
    public byte[] getTemplateData() {
        return templateData;
    }
    public void setTemplateData(byte[] templateData) {
        this.templateData = templateData;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
