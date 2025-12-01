// ResetPasswordRequest.java
package com.FinalProject.backend.Dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String username;
    private String code;
    private String newPassword;
}
