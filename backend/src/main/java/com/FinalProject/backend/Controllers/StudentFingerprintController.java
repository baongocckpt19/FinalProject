// FILE: src/main/java/com/FinalProject/backend/Controllers/StudentFingerprintController.java
package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Dto.StudentFingerprintInfoDto;
import com.FinalProject.backend.Service.FingerprintEnrollService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentFingerprintController {

    private final FingerprintEnrollService service;

    public StudentFingerprintController(FingerprintEnrollService service) {
        this.service = service;
    }

    // GET /api/students/{id}/fingerprint
    @GetMapping("/{studentId}/fingerprint")
    public ResponseEntity<StudentFingerprintInfoDto> getStudentFingerprintInfo(
            @PathVariable Integer studentId
    ) {
        return ResponseEntity.ok(service.getStudentFingerprintInfo(studentId));
    }
}
