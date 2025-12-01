// FILE: src/main/java/com/FinalProject/backend/Dto/DeviceRequest.java
package com.FinalProject.backend.Dto;

import lombok.Data;

@Data
public class DeviceRequest {
    private String deviceCode;
    private String deviceName;
    private String room;
    private Boolean isActive;   // optional, nếu null thì mặc định true
}
