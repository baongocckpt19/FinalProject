
package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Dto.AccountDto;
import com.FinalProject.backend.Dto.CustomUserDetail;
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
    // Lấy thông tin tài khoản hiện tại
    @GetMapping
    public ResponseEntity<?> getMe(
            @AuthenticationPrincipal CustomUserDetail userDetails
    ){
        AccountDto accountDto = accountService.getUser(userDetails.getId());
        return ResponseEntity.ok(Map.of("account", accountDto));
    }
    // Xóa tài khoản (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDelete(@PathVariable int id) {
        accountService.softDeleteAccount(id);
        return ResponseEntity.ok(Map.of("message", "Xóa tài khoản thành công"));
    }

}
