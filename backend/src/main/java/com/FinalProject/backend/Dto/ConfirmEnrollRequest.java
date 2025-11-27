// FILE: src/main/java/com/FinalProject/backend/Dto/ConfirmEnrollRequest.java
package com.FinalProject.backend.Dto;
import lombok.Data;
@Data
public class ConfirmEnrollRequest {
    private Integer studentId;
    private String sessionCode;

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public String getSessionCode() { return sessionCode; }
    public void setSessionCode(String sessionCode) { this.sessionCode = sessionCode; }
}
