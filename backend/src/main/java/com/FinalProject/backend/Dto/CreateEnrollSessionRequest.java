// FILE: src/main/java/com/FinalProject/backend/Dto/CreateEnrollSessionRequest.java
package com.FinalProject.backend.Dto;

public class CreateEnrollSessionRequest {
    private Integer studentId;
    // nếu muốn chọn trước thiết bị ở UI thì thêm deviceCode ở đây (tạm để null)
    private String deviceCode;

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public String getDeviceCode() { return deviceCode; }
    public void setDeviceCode(String deviceCode) { this.deviceCode = deviceCode; }
}
