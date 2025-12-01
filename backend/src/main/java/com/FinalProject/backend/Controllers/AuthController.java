package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Dto.AccountDto;
import com.FinalProject.backend.Dto.ForgotPasswordRequest;
import com.FinalProject.backend.Dto.RegisterRequest;
import com.FinalProject.backend.Dto.ResetPasswordRequest;
import com.FinalProject.backend.Models.Account;
import com.FinalProject.backend.Service.AccountService;
import com.FinalProject.backend.Service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Account account) {
        AccountDto acc = accountService.login(account.getUsername(), account.getPasswordHash());
        if (acc == null)
            return ResponseEntity.status(401).body("Sai tài khoản hoặc mật khẩu");
        String token = tokenService.generateToken(acc);
        return ResponseEntity.ok(Map.of(
                "token", token
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            accountService.register(req);
            return ResponseEntity.ok(Map.of(
                    "message", "Đăng ký thành công"
            ));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", ex.getMessage()
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of(
                    "message", "Có lỗi xảy ra khi đăng ký"
            ));
        }
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        try {
            accountService.requestPasswordReset(req.getUsername());
            return ResponseEntity.ok(Map.of(
                    "message", "Đã gửi mã xác nhận đến email (nếu tài khoản có email)."
            ));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", ex.getMessage()
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of(
                    "message", "Có lỗi xảy ra khi gửi mã xác nhận."
            ));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        try {
            accountService.resetPasswordWithCode(
                    req.getUsername(),
                    req.getCode(),
                    req.getNewPassword()
            );
            return ResponseEntity.ok(Map.of(
                    "message", "Đổi mật khẩu thành công."
            ));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", ex.getMessage()
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of(
                    "message", "Có lỗi xảy ra khi đổi mật khẩu."
            ));
        }
    }


}
