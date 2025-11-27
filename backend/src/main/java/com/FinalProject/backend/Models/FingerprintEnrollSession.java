// FILE: src/main/java/com/FinalProject/backend/Models/FingerprintEnrollSession.java
package com.FinalProject.backend.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "FingerprintEnrollSession")
public class FingerprintEnrollSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SessionId")
    private Integer sessionId;

    @Column(name = "SessionCode", nullable = false, unique = true)
    private String sessionCode;

    @Column(name = "StudentId", nullable = false)
    private Integer studentId;

    @Column(name = "DeviceId")
    private Integer deviceId;  // có thể null tới khi ESP báo

    @Column(name = "SensorSlot")
    private Integer sensorSlot; // slot ESP chọn

    @Lob
    @Column(name = "TemplateData")
    private byte[] templateData; // có sau khi ESP gửi

    @Column(name = "Status", nullable = false)
    private String status; // PENDING / RECEIVED / COMPLETED / CANCELED

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // getters & setters
    public Integer getSessionId() {
        return sessionId;
    }
    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }
    public String getSessionCode() {
        return sessionCode;
    }
    public void setSessionCode(String sessionCode) {
        this.sessionCode = sessionCode;
    }
    public Integer getStudentId() {
        return studentId;
    }
    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }
    public Integer getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }
    public Integer getSensorSlot() {
        return sensorSlot;
    }
    public void setSensorSlot(Integer sensorSlot) {
        this.sensorSlot = sensorSlot;
    }
    public byte[] getTemplateData() {
        return templateData;
    }
    public void setTemplateData(byte[] templateData) {
        this.templateData = templateData;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
