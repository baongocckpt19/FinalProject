//Đây là UserDetailDto.java
package com.FinalProject.backend.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailDto {
    private Integer accountId;
    private String username;
    private String roleName;

    private String fullName;
    private String email;
    private String userCode;

    private Integer teacherId;
    private Integer studentId;

    private String phone;
    private String address;
    private String dateOfBirth;
    private String gender;
    private Integer fingerCount;
}
