// src/main/java/com/FinalProject/backend/Controllers/TeacherAttendanceController.java
package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Dto.AttendanceCalendarDayDto;
import com.FinalProject.backend.Dto.AttendanceClassSummaryDto;
import com.FinalProject.backend.Dto.CustomUserDetail;
import com.FinalProject.backend.Service.TeacherAttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher-attendance")
public class TeacherAttendanceController {

    private final TeacherAttendanceService teacherAttendanceService;

    public TeacherAttendanceController(TeacherAttendanceService teacherAttendanceService) {
        this.teacherAttendanceService = teacherAttendanceService;
    }

    // 1) Lịch điểm danh (dùng cho Calendar View)
    // GET /api/teacher-attendance/calendar?start=2025-10-01&end=2025-10-31
    @GetMapping("/calendar")
    public ResponseEntity<?> getCalendar(
            @AuthenticationPrincipal CustomUserDetail userDetails,
            @RequestParam String start,
            @RequestParam String end
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate   = LocalDate.parse(end);

        List<AttendanceCalendarDayDto> data =
                teacherAttendanceService.getCalendarForAccount(userDetails.getId(), startDate, endDate);

        return ResponseEntity.ok(data);
    }

    // 2) Danh sách lớp có điểm danh trong 1 ngày
    // GET /api/teacher-attendance/day?date=2025-10-13
    @GetMapping("/day")
    public ResponseEntity<?> getClassesOfDay(
            @AuthenticationPrincipal CustomUserDetail userDetails,
            @RequestParam String date
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        LocalDate d = LocalDate.parse(date);
        List<AttendanceClassSummaryDto> list =
                teacherAttendanceService.getClassesForAccountAndDate(userDetails.getId(), d);

        return ResponseEntity.ok(list);
    }
}
