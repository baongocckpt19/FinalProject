package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Dto.CustomUserDetail;
import com.FinalProject.backend.Dto.TeacherDashboardStatsDto;
import com.FinalProject.backend.Service.TeacherDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher-dashboard")
public class TeacherDashboardController {

    private final TeacherDashboardService dashboardService;

    public TeacherDashboardController(TeacherDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // 1) STAT CARD (đang dùng)
    @GetMapping("/stats")
    public ResponseEntity<?> getStats(@AuthenticationPrincipal CustomUserDetail userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        TeacherDashboardStatsDto dto = dashboardService.getStatsForAccount(userDetails.getId());
        if (dto == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tài khoản không phải giảng viên"));
        }

        return ResponseEntity.ok(dto);
    }


}
