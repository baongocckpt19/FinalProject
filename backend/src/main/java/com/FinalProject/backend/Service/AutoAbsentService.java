// src/main/java/com/FinalProject/backend/Service/AutoAbsentService.java
package com.FinalProject.backend.Service;

import com.FinalProject.backend.Models.Attendance;
import com.FinalProject.backend.Models.ClassSchedule;
import com.FinalProject.backend.Repository.AttendanceRepository;
import com.FinalProject.backend.Repository.ClassScheduleRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AutoAbsentService {

    private final ClassScheduleRepository classScheduleRepository;
    private final AttendanceRepository attendanceRepository;

    /**
     * Mỗi 60 giây:
     * - Lấy các buổi học đã kết thúc (theo ngày + endTime)
     * - Với từng buổi:
     *   + Lấy danh sách sinh viên (JOIN StudentClass trong query findClassAttendanceBySchedule)
     *   + Nếu sinh viên chưa có Attendance -> tạo bản ghi "Vắng"
     *
     * Idempotent: Chạy nhiều lần cũng không tạo trùng, vì:
     * - Query trả attendanceId != null nếu đã có
     * - Bảng Attendance có UNIQUE(StudentId, ScheduleId)
     */
    public AutoAbsentService(ClassScheduleRepository classScheduleRepository,
                             AttendanceRepository attendanceRepository) {
        this.classScheduleRepository = classScheduleRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Scheduled(fixedDelay = 60000) // mỗi 60s chạy 1 lần, tuỳ bạn
    @Transactional
    public void fillAbsentForFinishedSchedules() {
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        // Chuyển sang chuỗi "HH:mm:ss" để dùng với CAST(:currentTime AS time)
        String currentTimeStr = currentTime.withNano(0).toString(); // ví dụ 14:30:05

        List<ClassSchedule> finishedSchedules =
                classScheduleRepository.findFinishedSchedules(today, currentTimeStr);

        for (ClassSchedule cs : finishedSchedules) {
            Integer scheduleId = cs.getScheduleId();

            List<Object[]> rows = attendanceRepository
                    .findClassAttendanceBySchedule(scheduleId);

            for (Object[] r : rows) {
                int i = 0;
                Integer classId   = ((Number) r[i++]).intValue(); // 0
                Integer schedId   = ((Number) r[i++]).intValue(); // 1
                Integer studentId = ((Number) r[i++]).intValue(); // 2

                i += 4; // fullName, username, email, phone bỏ qua

                Object attendanceIdObj = r[i++]; // 7

                // Nếu chưa có bản ghi Attendance -> tạo Vắng
                if (attendanceIdObj == null) {
                    Attendance a = new Attendance();
                    a.setStudentId(studentId);
                    a.setClassId(classId);
                    a.setScheduleId(schedId);
                    a.setStatus("Vắng");
                    a.setAttendanceTime(
                            cs.getEndTime() != null ? cs.getEndTime() : LocalTime.MIDNIGHT
                    );

                    attendanceRepository.save(a);
                }
            }
        }
    }
}
