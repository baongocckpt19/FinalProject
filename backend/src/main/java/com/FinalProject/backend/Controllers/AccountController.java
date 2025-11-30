package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Dto.AccountDto;
import com.FinalProject.backend.Dto.ChangePasswordRequest;
import com.FinalProject.backend.Dto.CheckPasswordRequest;
import com.FinalProject.backend.Dto.CustomUserDetail;
import com.FinalProject.backend.Dto.UpdateProfileRequest;
import com.FinalProject.backend.Service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    // =========================================================
    // 1. LẤY THÔNG TIN TÀI KHOẢN HIỆN TẠI
    //    GET /api/account
    // =========================================================
    @GetMapping
    public ResponseEntity<?> getMe(
            @AuthenticationPrincipal CustomUserDetail userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "message", "Chưa đăng nhập"
            ));
        }

        AccountDto accountDto = accountService.getUser(userDetails.getId());
        if (accountDto == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "message", "Không tìm thấy tài khoản"
            ));
        }

        return ResponseEntity.ok(Map.of("account", accountDto));
    }

    // =========================================================
    // 2. CẬP NHẬT THÔNG TIN CÁ NHÂN
    //    PUT /api/account/profile
    // =========================================================
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal CustomUserDetail userDetails,
            @RequestBody UpdateProfileRequest req
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "message", "Chưa đăng nhập"
            ));
        }

        try {
            accountService.updateProfile(userDetails.getId(), req);
            return ResponseEntity.ok(Map.of(
                    "message", "Cập nhật thông tin cá nhân thành công"
            ));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", ex.getMessage()
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of(
                    "message", "Có lỗi xảy ra khi cập nhật thông tin"
            ));
        }
    }

    // =========================================================
    // 3. ĐỔI MẬT KHẨU
    //    PUT /api/account/change-password
    // =========================================================
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal CustomUserDetail userDetails,
            @RequestBody ChangePasswordRequest req
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "message", "Chưa đăng nhập"
            ));
        }

        try {
            accountService.changePassword(userDetails.getId(), req);
            return ResponseEntity.ok(Map.of(
                    "message", "Đổi mật khẩu thành công"
            ));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", ex.getMessage()
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of(
                    "message", "Có lỗi xảy ra khi đổi mật khẩu"
            ));
        }
    }

    // =========================================================
    // 4. CHECK CURRENT PASSWORD (cho dấu ✓ / ✗ ở FE)
    //    POST /api/account/check-password
    //    Body: { "currentPassword": "..." }
    //    Response: { "valid": true/false }
    // =========================================================
    @PostMapping("/check-password")
    public ResponseEntity<?> checkCurrentPassword(
            @AuthenticationPrincipal CustomUserDetail userDetails,
            @RequestBody CheckPasswordRequest req
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "valid", false,
                    "message", "Chưa đăng nhập"
            ));
        }

        boolean valid = accountService.checkCurrentPassword(
                userDetails.getId(),
                req.getCurrentPassword()
        );

        return ResponseEntity.ok(Map.of("valid", valid));
    }

    // =========================================================
    // 5. XÓA MỀM TÀI KHOẢN
    //    DELETE /api/account/{id}
    // =========================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDelete(@PathVariable int id) {
        accountService.softDeleteAccount(id);
        return ResponseEntity.ok(Map.of(
                "message", "Xóa tài khoản thành công"
        ));
    }
}
