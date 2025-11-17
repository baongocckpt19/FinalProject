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

    private Integer teacherId;
    private Integer studentId;

    private String phone;
    private String address;
    private String dateOfBirth;
    private String gender;
    private Integer fingerCount;
}
