// TeacherController.java
package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Repository.TeacherRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                        "fullName", o[1]
                )
        );
    }
}
