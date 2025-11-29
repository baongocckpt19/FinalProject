// FILE: src/main/java/com/FinalProject/backend/Dto/StudentFingerprintInfoDto.java
package com.FinalProject.backend.Dto;

import java.util.List;
import lombok.Data;
@Data

public class StudentFingerprintInfoDto {
    private Integer studentId;
    private String fullName;
    private String username;
    private String email;
    private boolean hasFingerprint;
    private int fingerprintDevicesCount;
    private List<DeviceFingerprintInfoDto> devices;

    // getters & setters

}
