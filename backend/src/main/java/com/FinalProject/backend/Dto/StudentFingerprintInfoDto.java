// FILE: src/main/java/com/FinalProject/backend/Dto/StudentFingerprintInfoDto.java
package com.FinalProject.backend.Dto;

import java.util.List;

public class StudentFingerprintInfoDto {
    private Integer studentId;
    private String fullName;
    private String username;
    private String email;
    private boolean hasFingerprint;
    private int fingerprintDevicesCount;
    private List<DeviceFingerprintInfoDto> devices;

    // getters & setters
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isHasFingerprint() { return hasFingerprint; }
    public void setHasFingerprint(boolean hasFingerprint) { this.hasFingerprint = hasFingerprint; }
    public int getFingerprintDevicesCount() { return fingerprintDevicesCount; }
    public void setFingerprintDevicesCount(int fingerprintDevicesCount) { this.fingerprintDevicesCount = fingerprintDevicesCount; }
    public List<DeviceFingerprintInfoDto> getDevices() { return devices; }
    public void setDevices(List<DeviceFingerprintInfoDto> devices) { this.devices = devices; }
}
