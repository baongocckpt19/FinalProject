package com.FinalProject.backend.Dto;

import lombok.Data;

@Data
public class AccountDto {
    private int accountId;
    private String username;
    private String password;
    private int roleId;
    private String roleName;
}


