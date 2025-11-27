// FILE: src/main/java/com/FinalProject/backend/Models/Device.java
package com.FinalProject.backend.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "Device")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DeviceId")
    private Integer deviceId;

    @Column(name = "DeviceCode", nullable = false, unique = true)
    private String deviceCode;

    @Column(name = "DeviceName")
    private String deviceName;

    @Column(name = "Room")
    private String room;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;

    // getters & setters
    public Integer getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }
    public String getDeviceCode() {
        return deviceCode;
    }
    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }
    public String getDeviceName() {
        return deviceName;
    }
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    public String getRoom() {
        return room;
    }
    public void setRoom(String room) {
        this.room = room;
    }
    public Boolean getIsActive() {
        return isActive;
    }
    public void setIsActive(Boolean active) {
        isActive = active;
    }
}
