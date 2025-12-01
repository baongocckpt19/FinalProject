//ĐÂY LÀ CLASSCONTROLLER.JAVA
 package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Dto.ClassDetailDto;
import com.FinalProject.backend.Models.Clazz;
import com.FinalProject.backend.Service.ClassStudentService;
import com.FinalProject.backend.Service.ClassTableService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/classes")
public class ClassController {

    private final ClassTableService classTableService;
    private final ClassStudentService classStudentService;


    public ClassController(ClassTableService classTableService
                         , ClassStudentService classStudentService) {
        this.classTableService = classTableService;
        this.classStudentService = classStudentService;
    }
 // lấy danh sách lớp học
    @GetMapping
    public ResponseEntity<?> getClasses() {
        return ResponseEntity.ok(classTableService.getAllClasses());
    }
 //xóa lớp học (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClass(@PathVariable int id) {
        classTableService.softDelete(id);
        return ResponseEntity.ok().body("Xóa lớp thành công");
    }

    //export danh sách lớp học ra file excel
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportClassesCsv() {
        var classes = classTableService.getAllClasses();

        StringBuilder sb = new StringBuilder();

        sb.append("STT,Mã lớp,Tên lớp,Giảng viên,Số lượng sinh viên,Ngày tạo,Trạng thái,Số SV có vân tay\n");

        int stt = 1;
        for (var c : classes) {
            String code = safe(c.getClassCode());
            String name = safe(c.getClassName());
            String teacher = safe(c.getTeacherName());
            String count = c.getStudentCount() != null ? c.getStudentCount().toString() : "0";
            String created = safe(c.getCreatedDate());
            String status = (c.getStatus() != null && c.getStatus()) ? "Đã hoàn thành" : "Đang hoạt động";
            String fpCount = c.getFingerprintedCount() != null ? c.getFingerprintedCount().toString() : "0"; // NEW

            sb.append(stt++).append(",");
            sb.append(csv(code)).append(",");
            sb.append(csv(name)).append(",");
            sb.append(csv(teacher)).append(",");
            sb.append(csv(count)).append(",");
            sb.append(csv(created)).append(",");
            sb.append(csv(status)).append(",");
            sb.append(csv(fpCount)).append("\n"); // NEW
        }

        byte[] bom = new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF};
        byte[] data = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);

        byte[] result = new byte[bom.length + data.length];
        System.arraycopy(bom, 0, result, 0, bom.length);
        System.arraycopy(data, 0, result, bom.length, data.length);

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=classes.csv")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(result);
    }


    private String safe(String v) {
        return v == null ? "" : v;
    }

    private String csv(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            v = v.replace("\"", "\"\"");
            return "\"" + v + "\"";
        }
        return v;
    }
    //export danh sách sinh viên của lớp học theo id lớp
    @GetMapping("/{id}/export/students")
    public ResponseEntity<byte[]> exportStudentsOfClass(@PathVariable int id) {
        byte[] file = classTableService.exportStudentsOfClass(id);

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=class_" + id + "_students.csv")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(file);
    }

    //lấy chi tiết lớp học theo id
    @GetMapping("/{id}")
    public ResponseEntity<?> getClassDetail(@PathVariable int id) {
        ClassDetailDto dto = classTableService.getClassDetail(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    //cập nhật thông tin lớp học
    @PutMapping("/{id}")
    public ResponseEntity<?> updateClass(
            @PathVariable int id,
            @RequestBody Map<String, Object> body
    ) {
        String newCode = (String) body.get("classCode");
        String newName = (String) body.get("className");
        Integer teacherId = body.get("teacherId") != null ? (Integer) body.get("teacherId") : null;

        classTableService.updateClass(id, newCode, newName, teacherId);
        return ResponseEntity.ok(Map.of("message", "Cập nhật lớp học thành công"));
    }
    // tạo lớp học mới
    @PostMapping
    public ResponseEntity<?> createClass(@RequestBody Map<String, Object> body) {
        String classCode = (String) body.get("classCode");
        String className = (String) body.get("className");
        Integer teacherId = body.get("teacherId") != null ? (Integer) body.get("teacherId") : null;

        Clazz saved = classTableService.createClass(classCode, className, teacherId);

        return ResponseEntity.ok(Map.of(
                "message", "Tạo lớp học thành công",
                "classId", saved.getClassId()
        ));
    }

    // lấy danh sách sinh viên của lớp theo id lớp
    @GetMapping("/{id}/students")
    public ResponseEntity<?> getStudentsOfClass(@PathVariable int id) {
        var list = classTableService.getStudentsOfClass(id);
        return ResponseEntity.ok(list);
    }
    // xóa sinh viên khỏi lớp học (admin)
    @DeleteMapping("/{classId}/students/{studentId}")
    public ResponseEntity<?> removeStudentFromClass(
            @PathVariable int classId,
            @PathVariable int studentId
    ) {
        classStudentService.removeStudentFromClass(classId, studentId);
        return ResponseEntity.ok("Đã xóa sinh viên khỏi lớp");
    }

    // cập nhật trạng thái lớp học (active/inactive)
    @PutMapping("/{classId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable int classId,
            @RequestParam boolean status
    ) {
        classTableService.updateClassStatus(classId, status);
        return ResponseEntity.ok("Updated");
    }

    // export bảng điểm của lớp ra CSV
    @GetMapping("/{id}/export/grades")
    public ResponseEntity<byte[]> exportGradesOfClass(@PathVariable int id) {
        byte[] file = classTableService.exportGradesOfClass(id);

        if (file.length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=class_" + id + "_grades.csv")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(file);
    }

}

