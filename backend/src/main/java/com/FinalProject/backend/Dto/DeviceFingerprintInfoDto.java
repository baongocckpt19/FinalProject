// FILE: src/main/java/com/FinalProject/backend/Dto/DeviceFingerprintInfoDto.java
package com.FinalProject.backend.Dto;
import lombok.Data;

@Data
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

}
