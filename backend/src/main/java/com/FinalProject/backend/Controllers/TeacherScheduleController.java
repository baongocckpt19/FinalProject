// TeacherScheduleController.java
package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Dto.TeacherScheduleDto;
import com.FinalProject.backend.Service.ClassScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/schedules")
@RequiredArgsConstructor
public class TeacherScheduleController {

    private final ClassScheduleService classScheduleService;

    // GET /api/teacher/schedules?year=2025&month=11
    @GetMapping
    public List<TeacherScheduleDto> getSchedules(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return classScheduleService.getSchedulesForCurrentTeacher(year, month);
    }
}
