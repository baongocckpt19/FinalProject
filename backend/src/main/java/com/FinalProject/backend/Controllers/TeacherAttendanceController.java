package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Dto.AttendanceCalendarDayDto;
import com.FinalProject.backend.Dto.AttendanceClassSummaryDto;
import com.FinalProject.backend.Dto.AttendanceStudentDto;
import com.FinalProject.backend.Dto.CustomUserDetail;
import com.FinalProject.backend.Dto.StudentAttendanceDetailDto;
import com.FinalProject.backend.Service.TeacherAttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
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

    // 1) Calendar: GET /calendar?start=yyyy-MM-dd&end=yyyy-MM-dd
    @GetMapping("/calendar")
    public ResponseEntity<?> getCalendar(
            @AuthenticationPrincipal CustomUserDetail user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        List<AttendanceCalendarDayDto> days =
                teacherAttendanceService.getCalendarForAccount(user.getId(), start, end);

        return ResponseEntity.ok(days);
    }

    // 2) DS lớp trong 1 ngày: GET /day?date=yyyy-MM-dd
    @GetMapping("/day")
    public ResponseEntity<?> getClassesByDay(
            @AuthenticationPrincipal CustomUserDetail user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        List<AttendanceClassSummaryDto> list =
                teacherAttendanceService.getClassesForAccountAndDate(user.getId(), date);

        return ResponseEntity.ok(list);
    }

    // 3) DS sinh viên + trạng thái trong 1 lớp ở 1 ngày
    // GET /day/{classId}/students?date=yyyy-MM-dd
    @GetMapping("/day/{classId}/students")
    public ResponseEntity<?> getStudentsForClassAndDate(
            @AuthenticationPrincipal CustomUserDetail user,
            @PathVariable int classId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        List<AttendanceStudentDto> list =
                teacherAttendanceService.getStudentsForClassAndDate(user.getId(), classId, date);

        return ResponseEntity.ok(list);
    }

    // 4) Chi tiết 1 sinh viên trong lớp (modal)
    // GET /classes/{classId}/students/{studentId}
    @GetMapping("/classes/{classId}/students/{studentId}")
    public ResponseEntity<?> getStudentDetail(
            @AuthenticationPrincipal CustomUserDetail user,
            @PathVariable int classId,
            @PathVariable int studentId
    ) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        StudentAttendanceDetailDto dto =
                teacherAttendanceService.getStudentDetailForClass(user.getId(), classId, studentId);

        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    // 5) Cập nhật trạng thái điểm danh (Có mặt / Vắng / Muộn)
    // PUT /attendance/{attendanceId}/status { "status": "present|absent|late" }
    @PutMapping("/attendance/{attendanceId}/status")
    public ResponseEntity<?> updateAttendanceStatus(
            @AuthenticationPrincipal CustomUserDetail user,
            @PathVariable int attendanceId,
            @RequestBody Map<String, String> body
    ) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }
        String status = body.get("status");
        teacherAttendanceService.updateAttendanceStatus(user.getId(), attendanceId, status);
        return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái điểm danh thành công"));
    }

    // 6) Export CSV báo cáo điểm danh của lớp trong 1 ngày
// GET /day/{classId}/export?date=yyyy-MM-dd
    @GetMapping("/day/{classId}/export")
    public ResponseEntity<byte[]> exportAttendanceCsv(
            @AuthenticationPrincipal CustomUserDetail user,
            @PathVariable int classId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (user == null) {
            byte[] body = "Chưa đăng nhập".getBytes(StandardCharsets.UTF_8);
            return ResponseEntity.status(401)
                    .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                    .body(body);
        }

        // 1. Lấy summary của TẤT CẢ lớp trong ngày, rồi tìm đúng classId
        List<AttendanceClassSummaryDto> classList =
                teacherAttendanceService.getClassesForAccountAndDate(user.getId(), date);

        AttendanceClassSummaryDto summary = classList.stream()
                .filter(c -> c.getClassId() == classId)
                .findFirst()
                .orElse(null);

        if (summary == null) {
            byte[] body = "Không tìm thấy dữ liệu lớp trong ngày này".getBytes(StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=attendance_empty.csv")
                    .header(org.springframework.http.HttpHeaders.CONTENT_TYPE,
                            "text/csv; charset=UTF-8")
                    .body(body);
        }

        // 2. Lấy danh sách sinh viên điểm danh
        List<AttendanceStudentDto> students =
                teacherAttendanceService.getStudentsForClassAndDate(user.getId(), classId, date);

        // Nếu không có sinh viên => vẫn có thể xuất file "rỗng" nhưng giữ header
        if (students.isEmpty()) {
            StringBuilder sbEmpty = new StringBuilder();
            buildClassInfoBlock(sbEmpty, summary, date);
            sbEmpty.append("\n");
            sbEmpty.append("STT,Tên sinh viên,Mã số sinh viên,Thời gian điểm danh,Trạng thái\n");

            byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            byte[] data = sbEmpty.toString().getBytes(StandardCharsets.UTF_8);
            byte[] result = new byte[bom.length + data.length];
            System.arraycopy(bom, 0, result, 0, bom.length);
            System.arraycopy(data, 0, result, bom.length, data.length);

            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=attendance_empty.csv")
                    .header(org.springframework.http.HttpHeaders.CONTENT_TYPE,
                            "text/csv; charset=UTF-8")
                    .body(result);
        }

        // 3. Build CSV đầy đủ
        StringBuilder sb = new StringBuilder();

        // --- block thông tin lớp ---
        buildClassInfoBlock(sb, summary, date);
        sb.append("\n");

        // --- header bảng sinh viên ---
        sb.append("STT,Tên sinh viên,Mã số sinh viên,Thời gian điểm danh,Trạng thái\n");

        int stt = 1;
        for (AttendanceStudentDto s : students) {
            sb.append(stt++).append(",");
            sb.append(csv(s.getFullName())).append(",");
            sb.append(csv(String.valueOf(s.getStudentId()))).append(","); // MSSV
            sb.append(csv(s.getAttendanceTime() != null ? s.getAttendanceTime() : "")).append(",");
            sb.append(csv(mapStatusCodeToVi(s.getStatus()))).append("\n");
        }

        // Thêm BOM để Excel mở đúng UTF-8
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[bom.length + data.length];
        System.arraycopy(bom, 0, result, 0, bom.length);
        System.arraycopy(data, 0, result, bom.length, data.length);

        String fileName = String.format(
                "attendance_%s_%s.csv",
                summary.getClassCode(),
                date.toString()
        );

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + fileName)
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE,
                        "text/csv; charset=UTF-8")
                .body(result);
    }

    // Block thông tin lớp
    private static void buildClassInfoBlock(StringBuilder sb,
                                            AttendanceClassSummaryDto summary,
                                            LocalDate date) {
        // Tính (Có mặt + Muộn) để ra tỉ lệ
        int total = summary.getTotal() != null ? summary.getTotal() : 0;
        int present = summary.getPresent() != null ? summary.getPresent() : 0;
        int late = summary.getLate() != null ? summary.getLate() : 0;
        int absent = summary.getAbsent() != null ? summary.getAbsent() : 0;
        int effectivePresent = present + late;
        double rate = total > 0 ? (effectivePresent * 100.0 / total) : 0.0;

        sb.append("Tên lớp,").append(csv(summary.getClassName())).append("\n");
        sb.append("Mã lớp,").append(csv(summary.getClassCode())).append("\n");
        sb.append("Thời gian,").append(csv(summary.getTime())).append("\n");
        sb.append("Ngày học,").append(csv(date.toString())).append("\n");
        sb.append("Tổng sinh viên,").append(total).append("\n");
        sb.append("Có mặt,").append(present).append("\n");
        sb.append("Muộn,").append(late).append("\n");
        sb.append("Vắng mặt,").append(absent).append("\n");
        sb.append("Tỉ lệ điểm danh,").append(String.format("%.0f%%", rate)).append("\n");
    }

    private static String csv(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            v = v.replace("\"", "\"\"");
            return "\"" + v + "\"";
        }
        return v;
    }

    private static String mapStatusCodeToVi(String code) {
        if ("present".equals(code)) return "Có mặt";
        if ("late".equals(code)) return "Muộn";
        return "Vắng";
    }


}
