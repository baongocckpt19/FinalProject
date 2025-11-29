// src/main/java/com/FinalProject/backend/Controllers/StudentDashboardController.java
package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Dto.CustomUserDetail;
import com.FinalProject.backend.Dto.StudentDashboardDto;
import com.FinalProject.backend.Service.StudentDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * API trả về dữ liệu Dashboard cho SINH VIÊN đang đăng nhập.
 *
 * Endpoint:
 *  - GET /api/student/dashboard
 *    -> dựa vào token, lấy accountId của user hiện tại
 *    -> map sang StudentId
 *    -> trả về StudentDashboardDto.
 */
@RestController
@RequestMapping("/api/student/dashboard")
public class StudentDashboardController {

    @Autowired
    private StudentDashboardService studentDashboardService;

    /**
     * Lấy dashboard cho tài khoản hiện tại.
     *
     * @param userDetails CustomUserDetail lấy từ SecurityContext (JWT đã verify).
     */
    @GetMapping
    public ResponseEntity<?> getMyDashboard(
            @AuthenticationPrincipal CustomUserDetail userDetails
    ) {
        // id ở CustomUserDetail chính là AccountId (theo cách bạn đang dùng ở AccountController)
        Integer accountId = userDetails.getId();

        StudentDashboardDto dto = studentDashboardService.getDashboardForAccount(accountId);
        if (dto == null) {
            // Trường hợp không tìm thấy Student tương ứng với Account (có thể là role khác).
            return ResponseEntity.badRequest().body("Tài khoản này không phải sinh viên hoặc chưa được gán sinh viên.");
        }

        return ResponseEntity.ok(dto);
    }
}
