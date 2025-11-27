// FILE: src/main/java/com/FinalProject/backend/Dto/CreateEnrollSessionResponse.java
package com.FinalProject.backend.Dto;

public class CreateEnrollSessionResponse {
    private String sessionCode;

    public CreateEnrollSessionResponse(String sessionCode) {
        this.sessionCode = sessionCode;
    }

    public String getSessionCode() {
        return sessionCode;
    }
    public void setSessionCode(String sessionCode) {
        this.sessionCode = sessionCode;
    }
}
