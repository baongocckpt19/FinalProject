//PasswordResetToken.java
 package com.FinalProject.backend.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "PasswordResetToken")
@Data
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "AccountId", nullable = false)
    private Integer accountId;

    @Column(name = "Code", nullable = false, length = 20)
    private String code;   // mã 6 số

    @Column(name = "ExpiryDate", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "IsUsed", nullable = false)
    private boolean isUsed = false;
}
