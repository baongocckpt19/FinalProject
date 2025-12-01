package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Service.ClassStudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")  // nếu bạn chưa có thì thêm, cho chắc
public class ClassStudentController {

    private final ClassStudentService classStudentService;

    public ClassStudentController(ClassStudentService classStudentService) {
        this.classStudentService = classStudentService;
    }

    // 1) LẤY SV THEO studentId
    @GetMapping("/api/students/{id}")
    public ResponseEntity<?> getStudentInfo(@PathVariable("id") int studentId) {
        var info = classStudentService.getStudentInfo(studentId);
        if (info == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(info);
    }

    // 2) LẤY SV THEO MSSV (studentCode)
    @GetMapping("/api/students/by-code/{studentCode}")
    public ResponseEntity<?> getStudentByCode(@PathVariable String studentCode) {
        var student = classStudentService.getStudentInfoByCode(studentCode);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(student);
    }

    // 3) THÊM NHIỀU SINH VIÊN VÀO LỚP
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

    // 4) IMPORT CSV
    @PostMapping(
            value = "/api/classes/{classId}/students/import",
            consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> importStudentsCsv(
            @PathVariable int classId,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            var result = classStudentService.importStudentsFromCsv(classId, file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}
