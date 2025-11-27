// DeviceEnrollResultRequest.java
package com.FinalProject.backend.Dto;

/**
 * ESP32 gửi lên khi đã enroll xong và biết sensorSlot.
 */
public class DeviceEnrollResultRequest {
    private String sessionCode;
    private Integer sensorSlot;

    public String getSessionCode() {
        return sessionCode;
    }

    public void setSessionCode(String sessionCode) {
        this.sessionCode = sessionCode;
    }

    public Integer getSensorSlot() {
        return sensorSlot;
    }

    public void setSensorSlot(Integer sensorSlot) {
        this.sensorSlot = sensorSlot;
    }
}
