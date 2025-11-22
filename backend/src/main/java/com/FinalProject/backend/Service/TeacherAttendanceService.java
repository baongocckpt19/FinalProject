// src/main/java/com/FinalProject/backend/Service/TeacherAttendanceService.java
package com.FinalProject.backend.Service;

import com.FinalProject.backend.Dto.AttendanceCalendarDayDto;
import com.FinalProject.backend.Dto.AttendanceClassSummaryDto;
import com.FinalProject.backend.Repository.AttendanceRepository;
import com.FinalProject.backend.Repository.TeacherRepository;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class TeacherAttendanceService {

    private final TeacherRepository teacherRepository;
    private final AttendanceRepository attendanceRepository;

    public TeacherAttendanceService(TeacherRepository teacherRepository,
                                    AttendanceRepository attendanceRepository) {
        this.teacherRepository = teacherRepository;
        this.attendanceRepository = attendanceRepository;
    }

    // Lấy thống kê lịch điểm danh theo ngày trong khoảng [start, end]
    public List<AttendanceCalendarDayDto> getCalendarForAccount(int accountId,
                                                                LocalDate start,
                                                                LocalDate end) {
        Integer teacherId = teacherRepository.findTeacherIdByAccountId(accountId);
        if (teacherId == null) {
            return List.of();
        }

        List<Object[]> rows = attendanceRepository.findCalendarForTeacher(
                teacherId,
                Date.valueOf(start),
                Date.valueOf(end)
        );

        return rows.stream().map(r -> {
            AttendanceCalendarDayDto dto = new AttendanceCalendarDayDto();
            Date d = (Date) r[0];
            dto.setDate(d.toLocalDate().toString());                 // yyyy-MM-dd
            dto.setClassCount(((Number) r[1]).intValue());
            return dto;
        }).toList();
    }

    // Lấy danh sách lớp có điểm danh trong 1 ngày
    public List<AttendanceClassSummaryDto> getClassesForAccountAndDate(int accountId,
                                                                       LocalDate date) {
        Integer teacherId = teacherRepository.findTeacherIdByAccountId(accountId);
        if (teacherId == null) {
            return List.of();
        }

        List<Object[]> rows = attendanceRepository.findClassesForTeacherAndDate(
                teacherId,
                Date.valueOf(date)
        );

        return rows.stream().map(r -> {
            int i = 0;
            AttendanceClassSummaryDto dto = new AttendanceClassSummaryDto();
            dto.setClassId(((Number) r[i++]).intValue());     // 0
            dto.setClassCode(r[i++].toString());              // 1
            dto.setClassName(r[i++].toString());              // 2

            Time startTimeSql = (Time) r[i++];                // 3
            Time endTimeSql   = (Time) r[i++];                // 4
            LocalTime startTime = startTimeSql.toLocalTime();
            LocalTime endTime   = endTimeSql.toLocalTime();
            dto.setTime(String.format("%02d:%02d - %02d:%02d",
                    startTime.getHour(), startTime.getMinute(),
                    endTime.getHour(), endTime.getMinute()));

            Object statusRaw = r[i++];                        // 5
            boolean isActive;
            if (statusRaw instanceof Boolean b) {
                // hệ thống bạn đang dùng: 0/false = Hoạt động, 1/true = Tạm dừng/Đã kết thúc
                isActive = !b;
            } else if (statusRaw instanceof Number n) {
                isActive = n.intValue() == 0;
            } else {
                String s = statusRaw != null ? statusRaw.toString() : "";
                isActive = "0".equals(s) || "false".equalsIgnoreCase(s);
            }
            dto.setStatus(isActive ? "Đang hoạt động" : "Đã kết thúc");

            int total   = ((Number) r[i++]).intValue();       // 6
            int present = ((Number) r[i++]).intValue();       // 7
            int absent  = ((Number) r[i++]).intValue();       // 8
            int late    = ((Number) r[i++]).intValue();       // 9

            dto.setTotal(total);
            dto.setPresent(present);
            dto.setAbsent(absent);
            dto.setLate(late);

            double rate = total == 0 ? 0.0 : (present * 100.0 / total);
            dto.setRate(rate);

            return dto;
        }).toList();
    }
}
