//ĐÂY LÀ ConfirmEnrollResponse.JAVA
package com.FinalProject.backend.Dto;

import lombok.Data;

@Data
public class ConfirmEnrollResponse {
    private boolean success;
    private String message;
    private Integer sensorSlot;
    private Integer studentId;

    public ConfirmEnrollResponse() {
    }

    public ConfirmEnrollResponse(boolean success, String message, Integer sensorSlot, Integer studentId) {
        this.success = success;
        this.message = message;
        this.sensorSlot = sensorSlot;
        this.studentId = studentId;
    }
}
