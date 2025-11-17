package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Dto.AccountDto;
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
}
