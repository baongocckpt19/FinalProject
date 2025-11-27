// FILE: src/main/java/com/FinalProject/backend/Dto/UploadFromDeviceRequest.java
package com.FinalProject.backend.Dto;

/**
 * ESP32 gọi endpoint này sau khi enroll xong, gửi template + slot lên.
 */
public class UploadFromDeviceRequest {
    private String sessionCode;
    private String deviceCode;
    private Integer sensorSlot;
    private String templateBase64;

    public String getSessionCode() { return sessionCode; }
    public void setSessionCode(String sessionCode) { this.sessionCode = sessionCode; }
    public String getDeviceCode() { return deviceCode; }
    public void setDeviceCode(String deviceCode) { this.deviceCode = deviceCode; }
    public Integer getSensorSlot() { return sensorSlot; }
    public void setSensorSlot(Integer sensorSlot) { this.sensorSlot = sensorSlot; }
    public String getTemplateBase64() { return templateBase64; }
    public void setTemplateBase64(String templateBase64) { this.templateBase64 = templateBase64; }
}
