package com.FinalProject.backend.Dto;

import lombok.Data;

@Data
public class FingerprintVerifyRequest {
    private String deviceCode;   // vd: "ESP_ROOM_LAB1"
    private Integer sensorSlot;  // ID slot trên cảm biến
    private String timestamp;    // optional, ISO "2025-11-29T08:35:12", nếu null sẽ dùng giờ server
}
