// src/main/java/com/FinalProject/backend/Controllers/TeacherAttendanceController.java
package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Dto.ClassAttendanceStudentDto;
import com.FinalProject.backend.Dto.StudentAttendanceHistoryDto;
import com.FinalProject.backend.Service.TeacherAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/attendance")
@RequiredArgsConstructor
public class TeacherAttendanceController {

    private final TeacherAttendanceService teacherAttendanceService;

    // 1) Lấy chi tiết điểm danh lớp theo 1 buổi (scheduleId)
    @GetMapping("/schedule/{scheduleId}")
    public List<ClassAttendanceStudentDto> getClassAttendance(
            @PathVariable Integer scheduleId
    ) {
        return teacherAttendanceService.getClassAttendanceBySchedule(scheduleId);
    }

    // 2) Cập nhật / tạo mới trạng thái điểm danh cho 1 sinh viên trong buổi
    @PutMapping("/schedule/{scheduleId}/students/{studentId}")
    public ResponseEntity<?> updateAttendance(
            @PathVariable Integer scheduleId,
            @PathVariable Integer studentId,
            @RequestBody Map<String, String> body
    ) {
        String statusCode = body.get("status");
        teacherAttendanceService.upsertAttendance(scheduleId, studentId, statusCode);
        return ResponseEntity.ok(Map.of("message", "Cập nhật điểm danh thành công"));
    }

    // ⭐ 3) Xuất báo cáo CSV cho 1 buổi học
    //    GET /api/teacher/attendance/schedule/{scheduleId}/export
    @GetMapping("/schedule/{scheduleId}/export")
    public ResponseEntity<byte[]> exportAttendanceCsv(
            @PathVariable Integer scheduleId
    ) {
        byte[] bytes = teacherAttendanceService.exportAttendanceCsv(scheduleId);

        String fileName = "attendance_schedule_" + scheduleId + ".csv";

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        headers.setContentType(
                new MediaType("text", "csv", StandardCharsets.UTF_8)
        );

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(bytes);
    }

    // 4) Lịch sử điểm danh của 1 sinh viên trong 1 lớp
    //    GET /api/teacher/attendance/class/{classId}/student/{studentId}/history
    @GetMapping("/class/{classId}/student/{studentId}/history")
    public List<StudentAttendanceHistoryDto> getStudentHistory(
            @PathVariable Integer classId,
            @PathVariable Integer studentId
    ) {
        return teacherAttendanceService.getStudentHistory(classId, studentId);
    }


}
