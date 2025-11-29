package com.FinalProject.backend.Dto;

import lombok.Data;

@Data
public class FingerprintVerifyResponse {

    private boolean success;
    private String message;

    // code cho ESP / FE:
    // "present" | "late" | "duplicate" | "fail"
    private String status;

    private Integer studentId;
    private String fullName;
    private Integer classId;
    private Integer scheduleId;
    private String attendanceTime; // "HH:mm:ss"
}
