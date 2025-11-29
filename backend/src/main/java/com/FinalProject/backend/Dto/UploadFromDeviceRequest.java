// FILE: src/main/java/com/FinalProject/backend/Dto/UploadFromDeviceRequest.java
package com.FinalProject.backend.Dto;

/**
 * ESP32 gọi endpoint này sau khi enroll xong, gửi template + slot lên.
 */
import lombok.Data;
@Data
public class UploadFromDeviceRequest {
    private String sessionCode;
    private String deviceCode;
    private Integer sensorSlot;
    private String templateBase64;

}
