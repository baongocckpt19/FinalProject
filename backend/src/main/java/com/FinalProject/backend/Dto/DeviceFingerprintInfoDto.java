// FILE: src/main/java/com/FinalProject/backend/Dto/DeviceFingerprintInfoDto.java
package com.FinalProject.backend.Dto;

public class DeviceFingerprintInfoDto {
    private Integer deviceId;
    private String deviceCode;
    private String deviceName;
    private String room;
    private Integer sensorSlot;

    public DeviceFingerprintInfoDto() {}

    public DeviceFingerprintInfoDto(Integer deviceId, String deviceCode, String deviceName,
                                    String room, Integer sensorSlot) {
        this.deviceId = deviceId;
        this.deviceCode = deviceCode;
        this.deviceName = deviceName;
        this.room = room;
        this.sensorSlot = sensorSlot;
    }

    // getters & setters
    public Integer getDeviceId() { return deviceId; }
    public void setDeviceId(Integer deviceId) { this.deviceId = deviceId; }
    public String getDeviceCode() { return deviceCode; }
    public void setDeviceCode(String deviceCode) { this.deviceCode = deviceCode; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
    public Integer getSensorSlot() { return sensorSlot; }
    public void setSensorSlot(Integer sensorSlot) { this.sensorSlot = sensorSlot; }
}
