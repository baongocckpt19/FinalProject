package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Dto.StudentFingerprintInfoDto;
import com.FinalProject.backend.Models.Student;
import com.FinalProject.backend.Repository.StudentRepository;
import com.FinalProject.backend.Service.FingerprintEnrollService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class StudentFingerprintController {

    private final StudentRepository studentRepository;
    private final FingerprintEnrollService fingerprintEnrollService;

    public StudentFingerprintController(
            StudentRepository studentRepository,
            FingerprintEnrollService fingerprintEnrollService
    ) {
        this.studentRepository = studentRepository;
        this.fingerprintEnrollService = fingerprintEnrollService;
    }

    /**
     * GET /api/students/by-code/{studentCode}/fingerprint
     *  - Dùng MSSV (studentCode) để tìm sinh viên
     *  - Sau đó gọi service getStudentFingerprintInfo(studentId)
     */
    @GetMapping("/students/by-code/{studentCode}/fingerprint")
    public ResponseEntity<StudentFingerprintInfoDto> getStudentFingerprintByCode(
            @PathVariable String studentCode
    ) {
        // Tìm entity Student theo MSSV
        Student student = studentRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy sinh viên với MSSV = " + studentCode
                ));

        // Dùng service đã có sẵn để build DTO (theo studentId)
        StudentFingerprintInfoDto dto =
                fingerprintEnrollService.getStudentFingerprintInfo(student.getStudentId());

        return ResponseEntity.ok(dto);
    }
}
