package com.FinalProject.backend.Service;

import com.FinalProject.backend.Dto.AccountDto;
import com.FinalProject.backend.Models.Account;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    // Sinh token mới
    public String generateToken(AccountDto user) {
        Date now = new Date();
        long expiryInMs = 1000L * 60 * 60 * 24 * 30; // 30 ngày
        Date expiryDate = new Date(now.getTime() + expiryInMs);

        return Jwts.builder()
                .claim("id", user.getAccountId())
                .claim("role", user.getRoleName())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    // Kiểm tra token hợp lệ (chữ ký, hết hạn)
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Lấy role từ token
    public String extractRole(String token) {
        return (String) Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .get("role");
    }

    // Lấy id từ token
    public int extractUserId(String token) {
        return (Integer) Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .get("id");
    }
}

