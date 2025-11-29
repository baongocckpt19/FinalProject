// FILE: src/main/java/com/FinalProject/backend/Dto/CreateEnrollSessionRequest.java
package com.FinalProject.backend.Dto;
import lombok.Data;
@Data
public class CreateEnrollSessionRequest {
    private Integer studentId;
    // nếu muốn chọn trước thiết bị ở UI thì thêm deviceCode ở đây (tạm để null)
    private String deviceCode;


}
