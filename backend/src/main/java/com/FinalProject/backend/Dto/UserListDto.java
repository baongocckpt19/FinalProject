//Đây là UserListDto.java
package com.FinalProject.backend.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserListDto {
    private Integer accountId;
    private String fullName;
    private String username;
    private String roleName;
    private String email;
    private Integer teacherId;
    private Integer studentId;
    // 👇 thêm mấy field để export + modal
    private String phone;
    private String address;
    private String dateOfBirth;
    private String gender;
    private Integer fingerCount;
    private String userCode;
}
