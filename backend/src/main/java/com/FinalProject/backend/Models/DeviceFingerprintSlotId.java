// FILE: src/main/java/com/FinalProject/backend/Models/DeviceFingerprintSlotId.java
package com.FinalProject.backend.Models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DeviceFingerprintSlotId implements Serializable {

    @Column(name = "DeviceId")
    private Integer deviceId;

    @Column(name = "StudentId")
    private Integer studentId;

    public Integer getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }
    public Integer getStudentId() {
        return studentId;
    }
    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceFingerprintSlotId)) return false;
        DeviceFingerprintSlotId that = (DeviceFingerprintSlotId) o;
        return Objects.equals(deviceId, that.deviceId) &&
                Objects.equals(studentId, that.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, studentId);
    }
}
