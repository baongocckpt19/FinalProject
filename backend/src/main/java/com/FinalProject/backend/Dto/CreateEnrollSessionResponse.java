// FILE: src/main/java/com/FinalProject/backend/Dto/CreateEnrollSessionResponse.java
package com.FinalProject.backend.Dto;
import lombok.Data;
@Data

public class CreateEnrollSessionResponse {
    private String sessionCode;

    public CreateEnrollSessionResponse(String sessionCode) {
        this.sessionCode = sessionCode;
    }
}
