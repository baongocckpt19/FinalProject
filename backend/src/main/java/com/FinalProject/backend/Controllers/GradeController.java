package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Dto.StudentGradeDto;
import com.FinalProject.backend.Service.GradeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/classes/{classId}/grades")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    // Lấy danh sách điểm của 1 lớp
    @GetMapping
    public ResponseEntity<?> getGrades(@PathVariable int classId) {
        return ResponseEntity.ok(gradeService.getGradesForClass(classId));
    }

    // Cập nhật / lưu điểm 1 sinh viên trong lớp
    @PutMapping("/{studentId}")
    public ResponseEntity<?> updateStudentGrade(
            @PathVariable int classId,
            @PathVariable int studentId,
            @RequestBody StudentGradeDto body
    ) {
        gradeService.saveGradeForStudent(classId, studentId, body);
        return ResponseEntity.ok(Map.of("message", "Lưu điểm thành công"));
    }

    // Lưu tất cả điểm của lớp
    @PutMapping
    public ResponseEntity<?> updateAllGrades(
            @PathVariable int classId,
            @RequestBody List<StudentGradeDto> list
    ) {
        gradeService.saveAllGradesForClass(classId, list);
        return ResponseEntity.ok(Map.of("message", "Lưu tất cả điểm thành công"));
    }

    // 🔥 IMPORT ĐIỂM TỪ CSV
    @PostMapping(
            value = "/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> importGrades(
            @PathVariable int classId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("gradeType") String gradeType
    ) {
        try {
            var result = gradeService.importGradesFromCsv(classId, gradeType, file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
