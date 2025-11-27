// src/main/java/com/FinalProject/backend/Controller/ClassScheduleController.java
package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Dto.ClassScheduleDto;
import com.FinalProject.backend.Service.ClassScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/class-schedule")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // nếu bạn đã cấu hình CORS global thì có thể bỏ
public class ClassScheduleController {

    private final ClassScheduleService classScheduleService;

    /**
     * Lấy danh sách lịch học của 1 lớp (dùng cho modal Lịch học trong Admin)
     * GET /api/class-schedule/by-class/{classId}
     */
    @GetMapping("/by-class/{classId}")
    public ResponseEntity<List<ClassScheduleDto>> getByClass(@PathVariable Integer classId) {
        List<ClassScheduleDto> list = classScheduleService.getSchedulesByClassId(classId);
        return ResponseEntity.ok(list);
    }

    /**
     * Tạo mới lịch học
     * POST /api/class-schedule
     * Body: ClassScheduleDto (classId, scheduleDate, startTime, endTime, room, isActive)
     */
    @PostMapping
    public ResponseEntity<String> create(@RequestBody ClassScheduleDto dto) {
        classScheduleService.createSchedule(dto);
        return ResponseEntity.ok("Created");
    }

    /**
     * Cập nhật lịch học
     * PUT /api/class-schedule/{scheduleId}
     */
    @PutMapping("/{scheduleId}")
    public ResponseEntity<String> update(
            @PathVariable Integer scheduleId,
            @RequestBody ClassScheduleDto dto
    ) {
        classScheduleService.updateSchedule(scheduleId, dto);
        return ResponseEntity.ok("Updated");
    }

    /**
     * Bật/tắt IsActive (tạm hoãn)
     * PUT /api/class-schedule/{scheduleId}/active?isActive=true/false
     */
    @PutMapping("/{scheduleId}/active")
    public ResponseEntity<String> updateActive(
            @PathVariable Integer scheduleId,
            @RequestParam("isActive") Boolean isActive
    ) {
        classScheduleService.updateActive(scheduleId, isActive);
        return ResponseEntity.ok("Updated");
    }

    /**
     * Soft delete lịch học (IsDeleted = 1)
     * DELETE /api/class-schedule/{scheduleId}
     */
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<String> delete(@PathVariable Integer scheduleId) {
        classScheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.ok("Deleted");
    }
}
