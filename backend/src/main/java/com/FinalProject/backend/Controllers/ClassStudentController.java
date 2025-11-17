//API lấy SV và API thêm SV vào lớp
package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Service.ClassStudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
public class ClassStudentController {

    private final ClassStudentService classStudentService;

    public ClassStudentController(ClassStudentService classStudentService) {
        this.classStudentService = classStudentService;
    }

    // GET /api/students/{studentId}
    // lấy thông tin sinh viên theo id
    @GetMapping("/api/students/{id}")
    public ResponseEntity<?> getStudentInfo(@PathVariable("id") int studentId) {
        var info = classStudentService.getStudentInfo(studentId);
        if (info == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(info);
    }

    // POST /api/classes/{classId}/students
    // thêm nhiều sinh viên vào lớp
    @PostMapping("/api/classes/{classId}/students")
    public ResponseEntity<?> addStudentsToClass(
            @PathVariable int classId,
            @RequestBody Map<String, Object> body
    ) {
        @SuppressWarnings("unchecked")
        List<Integer> studentIds = (List<Integer>) body.get("studentIds");
        classStudentService.addStudentsToClass(classId, studentIds);
        return ResponseEntity.ok(Map.of("message", "Đã thêm sinh viên vào lớp"));
    }

    // 3) IMPORT CSV
    // ClassStudentController.java
    @PostMapping(
            value = "/api/classes/{classId}/students/import",
            consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> importStudentsCsv(
            @PathVariable int classId,
            @RequestParam("file") MultipartFile file   // <-- đổi từ @RequestPart sang @RequestParam
    ) {
        try {
            var result = classStudentService.importStudentsFromCsv(classId, file);
            return ResponseEntity.ok(result); // trả về cả thống kê chi tiết
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

}
