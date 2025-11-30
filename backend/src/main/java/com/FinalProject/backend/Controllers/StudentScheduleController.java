// src/main/java/com/FinalProject/backend/Controllers/StudentScheduleController.java
package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Dto.StudentScheduleDto;
import com.FinalProject.backend.Service.ClassScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/schedules")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StudentScheduleController {

    private final ClassScheduleService classScheduleService;

    // GET /api/student/schedules?year=2025&month=11
    @GetMapping
    public List<StudentScheduleDto> getStudentSchedules(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return classScheduleService.getSchedulesForCurrentStudent(year, month);
    }
}
