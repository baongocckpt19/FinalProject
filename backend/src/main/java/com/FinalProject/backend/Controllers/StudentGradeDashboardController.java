// src/main/java/com/FinalProject/backend/Controllers/StudentGradeDashboardController.java

//=======================API dành cho sinh viên xem bảng điểm của mình=======================
package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Dto.AccountDto;
import com.FinalProject.backend.Dto.CustomUserDetail;
import com.FinalProject.backend.Dto.StudentClassGradeDto;
import com.FinalProject.backend.Models.Student;
import com.FinalProject.backend.Repository.StudentRepository;
import com.FinalProject.backend.Service.GradeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/grades")
public class StudentGradeDashboardController {

    private final GradeService gradeService;
    private final StudentRepository studentRepository;

    public StudentGradeDashboardController(GradeService gradeService,
                                           StudentRepository studentRepository) {
        this.gradeService = gradeService;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/classes")
    public ResponseEntity<?> getMyClassesWithGrades() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetail userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Chưa đăng nhập"));
        }

        // tuỳ CustomUserDetail của bạn mà lấy cho đúng:
        // giả sử có hàm getAccount() trả về AccountDto
        AccountDto account = userDetails.getAccount();
        Integer accountId = account.getAccountId();

        Student student = studentRepository.findByAccountId(accountId);
        if (student == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Không tìm thấy sinh viên tương ứng với tài khoản"));
        }

        List<StudentClassGradeDto> list =
                gradeService.getClassesWithGradesForStudent(student.getStudentId());

        return ResponseEntity.ok(list);
    }
}
