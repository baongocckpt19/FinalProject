// DeviceEnrollResultRequest.java
package com.FinalProject.backend.Dto;
import lombok.Data;

/**
 * ESP32 gửi lên khi đã enroll xong và biết sensorSlot.
 */
@Data

public class DeviceEnrollResultRequest {
    private String sessionCode;
    private Integer sensorSlot;
}
