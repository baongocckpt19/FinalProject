package com.FinalProject.backend.Dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class AccountDto {
    private int accountId;
    private String username;
    @JsonIgnore           // 👈 thêm dòng này
    private String password;
    // vẫn giữ để login dùng
    private int roleId;
    private String roleName;
    private String fullName;
    //thêm userCode để hiển thị mã số sinh viên/giáo viên
    private String userCode;

    private Integer studentId;
    private Integer teacherId;
    private String email;
    private String phone;
    private String address;
    private String dateOfBirth;
    private String gender;
}
