package com.FinalProject.backend.Dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String birthDate; // "yyyy-MM-dd"
    private String gender;    // "Nam" / "Nữ" / "Khác" / null
}
