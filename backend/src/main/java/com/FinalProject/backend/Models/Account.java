package com.FinalProject.backend.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Account")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AccountId")
    private int accountId;

    @Column(name = "Username")
    private String username;

    @Column(name = "PasswordHash")
    private String passwordHash;

    @Column(name = "RoleId", nullable = false)
    private int roleId;

    // isDeleted
    @Column(name = "isDeleted", nullable = false)
    private boolean isDeleted = false; // false = 0 = đang hoạt động
}
