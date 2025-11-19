
package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Dto.ClassListDto;
import com.FinalProject.backend.Dto.CustomUserDetail;
import com.FinalProject.backend.Repository.TeacherRepository;
import com.FinalProject.backend.Service.ClassTableService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher-classes")
public class TeacherClassController {

    private final TeacherRepository teacherRepository;
    private final ClassTableService classTableService;

    public TeacherClassController(TeacherRepository teacherRepository,
                                  ClassTableService classTableService) {
        this.teacherRepository = teacherRepository;
        this.classTableService = classTableService;
    }

    // 1) Lấy danh sách lớp của giảng viên đang đăng nhập
    @GetMapping
    public ResponseEntity<?> getMyClasses(
            @AuthenticationPrincipal CustomUserDetail userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        Integer teacherId = teacherRepository.findTeacherIdByAccountId(userDetails.getId());
        if (teacherId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tài khoản không phải giảng viên"));
        }

        List<ClassListDto> classes = classTableService.getClassesForTeacher(teacherId);
        return ResponseEntity.ok(classes);
    }

    // 2) Export CSV các lớp của giảng viên đang đăng nhập
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportMyClasses(
            @AuthenticationPrincipal CustomUserDetail userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401)
                    .header("Content-Type", "text/plain; charset=UTF-8")
                    .body("Chưa đăng nhập".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        Integer teacherId = teacherRepository.findTeacherIdByAccountId(userDetails.getId());
        if (teacherId == null) {
            return ResponseEntity.badRequest()
                    .header("Content-Type", "text/plain; charset=UTF-8")
                    .body("Tài khoản không phải giảng viên".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        List<ClassListDto> classes = classTableService.getClassesForTeacher(teacherId);

        StringBuilder sb = new StringBuilder();
        sb.append("STT,Mã lớp,Tên lớp,Giảng viên,Số lượng sinh viên,Ngày tạo,Trạng thái,Số SV có vân tay\n");

        int stt = 1;
        for (ClassListDto c : classes) {
            String code = safe(c.getClassCode());
            String name = safe(c.getClassName());
            String teacher = safe(c.getTeacherName());
            String count = c.getStudentCount() != null ? c.getStudentCount().toString() : "0";
            String created = safe(c.getCreatedDate());

            // Ở hệ thống của bạn: Status = false/0 là đang hoạt động, true/1 là tạm dừng/đã hoàn thành
            String status = (c.getStatus() != null && c.getStatus()) ? "Tạm dừng" : "Hoạt động";

            String fpCount = c.getFingerprintedCount() != null ? c.getFingerprintedCount().toString() : "0";

            sb.append(stt++).append(",");
            sb.append(csv(code)).append(",");
            sb.append(csv(name)).append(",");
            sb.append(csv(teacher)).append(",");
            sb.append(csv(count)).append(",");
            sb.append(csv(created)).append(",");
            sb.append(csv(status)).append(",");
            sb.append(csv(fpCount)).append("\n");
        }

        byte[] bom = new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF};
        byte[] data = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] result = new byte[bom.length + data.length];
        System.arraycopy(bom, 0, result, 0, bom.length);
        System.arraycopy(data, 0, result, bom.length, data.length);

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=my-classes.csv")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(result);
    }

    private static String safe(String v) {
        return v == null ? "" : v;
    }

    private static String csv(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            v = v.replace("\"", "\"\"");
            return "\"" + v + "\"";
        }
        return v;
    }
}
