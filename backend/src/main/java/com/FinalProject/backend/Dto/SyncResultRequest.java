// FILE: src/main/java/com/FinalProject/backend/Dto/SyncResultRequest.java
package com.FinalProject.backend.Dto;

import lombok.Data;

@Data
public class SyncResultRequest {
    private String sessionCode;  // SYNC-xxxxx
    private Integer sensorSlot;  // slot mà ESP đã lưu trên sensor
}
