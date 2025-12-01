// TeacherController.java
package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Repository.TeacherRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherRepository teacherRepository;

    public TeacherController(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }
    //lấy danh sách giáo viên (id, fullName)
    @GetMapping("/{id}")
    public ResponseEntity<?> getTeacher(@PathVariable int id) {
        Object r = teacherRepository.findTeacherShortById(id);
        if (r == null) return ResponseEntity.notFound().build();
        Object[] o = (Object[]) r;
        return ResponseEntity.ok(
                java.util.Map.of(
                        "teacherId", o[0],
                        "fullName",  o[1],
                        "teacherCode", o[2]   // =
                )
        );
    }
    // ⭐ NEW: GET /api/teachers/by-code/{teacherCode}
    @GetMapping("/by-code/{teacherCode}")
    public ResponseEntity<?> getTeacherByCode(@PathVariable String teacherCode) {
        var opt = teacherRepository.findByTeacherCode(teacherCode);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var t = opt.get();
        return ResponseEntity.ok(
                Map.of(
                        "teacherId",   t.getTeacherId(),
                        "fullName",    t.getFullName(),
                        "teacherCode", t.getTeacherCode()
                )
        );
    }
//    // GET /api/teachers/by-code/{teacherCode}
//    @GetMapping("/by-code/{teacherCode}")
//    public ResponseEntity<?> getTeacherByCode(@PathVariable String teacherCode) {
//        var info = ClassTableService.getTeacherInfoByCode(teacherCode);
//        if (info == null) {
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok(info);
//    }



}
