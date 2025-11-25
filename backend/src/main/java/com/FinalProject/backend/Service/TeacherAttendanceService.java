package com.FinalProject.backend.Service;

import com.FinalProject.backend.Dto.AttendanceCalendarDayDto;
import com.FinalProject.backend.Dto.AttendanceClassSummaryDto;
import com.FinalProject.backend.Dto.AttendanceStudentDto;
import com.FinalProject.backend.Dto.StudentAttendanceDetailDto;
import com.FinalProject.backend.Dto.StudentAttendanceHistoryDto;
import com.FinalProject.backend.Models.Clazz;
import com.FinalProject.backend.Repository.AttendanceRepository;
import com.FinalProject.backend.Repository.ClassRepository;
import com.FinalProject.backend.Repository.StudentRepository;
import com.FinalProject.backend.Repository.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class TeacherAttendanceService {

    private final TeacherRepository teacherRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final ClassRepository classRepository;

    public TeacherAttendanceService(TeacherRepository teacherRepository,
                                    AttendanceRepository attendanceRepository,
                                    StudentRepository studentRepository,
                                    ClassRepository classRepository) {
        this.teacherRepository = teacherRepository;
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.classRepository = classRepository;
    }

    // ====== 1) Calendar theo khoảng ngày ======
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

    // ====== 2) Danh sách lớp có điểm danh trong 1 ngày ======
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
                isActive = !b; // 0/false = hoạt động, 1/true = kết thúc
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

    // ===================== 3) DS SV trong 1 lớp ở 1 ngày =====================

    public List<AttendanceStudentDto> getStudentsForClassAndDate(
            int accountId,
            int classId,
            LocalDate date
    ) {
        // kiểm tra lớp có phải của GV đang đăng nhập không (an toàn hơn)
        Integer teacherId = teacherRepository.findTeacherIdByAccountId(accountId);
        if (teacherId == null) return List.of();

        Optional<Clazz> clazzOpt = classRepository.findById(classId);
        if (clazzOpt.isEmpty()) return List.of();
        Clazz clazz = clazzOpt.get();
        if (clazz.getTeacherId() == null || !clazz.getTeacherId().equals(teacherId)) {
            // không phải lớp của GV này
            return List.of();
        }

        List<Object[]> rows = attendanceRepository
                .findStudentAttendanceForClassAndDate(classId, Date.valueOf(date));

        return rows.stream().map(r -> {
            int i = 0;
            AttendanceStudentDto dto = new AttendanceStudentDto();
            dto.setAttendanceId(((Number) r[i++]).intValue());     // 0
            dto.setStudentId(((Number) r[i++]).intValue());        // 1
            dto.setFullName((String) r[i++]);                      // 2
            dto.setUsername((String) r[i++]);                      // 3

            String statusVi = (String) r[i++];                     // 4
            dto.setStatus(mapStatusViToCode(statusVi));            // present/absent/late

            Time time = (Time) r[i++];                             // 5
            dto.setAttendanceTime(time != null ? time.toString() : null);

            Integer totalSessions   = asInt(r[i++]);               // 6
            Integer presentSessions = asInt(r[i++]);               // 7
            Integer lateSessions    = asInt(r[i++]);               // 8
            Integer absentSessions  = asInt(r[i++]);               // 9

            dto.setTotalSessions(totalSessions != null ? totalSessions : 0);
            dto.setPresentSessions(presentSessions != null ? presentSessions : 0);
            dto.setLateSessions(lateSessions != null ? lateSessions : 0);
            dto.setAbsentSessions(absentSessions != null ? absentSessions : 0);

            int total = dto.getTotalSessions();
            int joined = dto.getPresentSessions() + dto.getLateSessions();
            double rate = (total == 0) ? 0.0 : (joined * 100.0 / total);
            dto.setAttendanceRate(rate);

            return dto;
        }).toList();
    }

    // ===================== 4) Chi tiết 1 sinh viên trong lớp =====================

    public StudentAttendanceDetailDto getStudentDetailForClass(
            int accountId,
            int classId,
            int studentId
    ) {
        Integer teacherId = teacherRepository.findTeacherIdByAccountId(accountId);
        if (teacherId == null) return null;

        Optional<Clazz> clazzOpt = classRepository.findById(classId);
        if (clazzOpt.isEmpty()) return null;
        Clazz clazz = clazzOpt.get();
        if (clazz.getTeacherId() == null || !clazz.getTeacherId().equals(teacherId)) {
            return null;
        }

        // Thông tin lớp
        String classCode = clazz.getClassCode();
        String className = clazz.getClassName();

        // Thông tin sinh viên + username, email, phone...
        Object info = studentRepository.findStudentInfoById(studentId);
        if (info == null) return null;
        Object[] o = (Object[]) info;
        int i = 0;
        Integer sId        = (Integer) o[i++]; // 0
        String fullName    = (String)  o[i++]; // 1
        String username    = (String)  o[i++]; // 2
        String dateOfBirth = (String)  o[i++]; // 3
        String gender      = (String)  o[i++]; // 4
        String address     = (String)  o[i++]; // 5
        String email       = (String)  o[i++]; // 6
        String phone       = (String)  o[i++]; // 7

        // Lịch sử điểm danh
        List<Object[]> rows = attendanceRepository
                .findAttendanceHistoryForStudentInClass(classId, studentId);

        final int[] totalSessions = {0};
        final int[] present = {0};
        final int[] late = { 0 };
        final int[] absent = { 0 };

        List<StudentAttendanceHistoryDto> history = rows.stream().map(r -> {
            StudentAttendanceHistoryDto h = new StudentAttendanceHistoryDto();
            int j = 0;
            h.setAttendanceId(((Number) r[j++]).intValue()); // 0
            Date d = (Date) r[j++];                          // 1
            Time sessionStart = (Time) r[j++];               // 2
            Time sessionEnd   = (Time) r[j++];               // 3
            String statusVi   = (String) r[j++];             // 4
            Time attTime      = (Time) r[j++];               // 5

            h.setDate(d.toLocalDate().toString());
            h.setSessionTime(String.format("%02d:%02d - %02d:%02d",
                    sessionStart.toLocalTime().getHour(), sessionStart.toLocalTime().getMinute(),
                    sessionEnd.toLocalTime().getHour(), sessionEnd.toLocalTime().getMinute()));
            String statusCode = mapStatusViToCode(statusVi);
            h.setStatus(statusCode);
            h.setAttendanceTime(attTime != null ? attTime.toString() : null);

            // thống kê
            totalSessions[0]++;
            switch (statusCode) {
                case "present" -> present[0]++;
                case "late"    -> late[0]++;
                case "absent"  -> absent[0]++;
            }

            return h;
        }).toList();

        // Nếu muốn tổng buổi là tổng buổi của lớp, dùng countTotalSessionsForClass
        Integer totalFromClass = attendanceRepository.countTotalSessionsForClass(classId);
        if (totalFromClass != null && totalFromClass > totalSessions[0]) {
            totalSessions[0] = totalFromClass;
        }

        int joined = present[0] + late[0];
        double rate = (totalSessions[0] == 0) ? 0.0 : (joined * 100.0 / totalSessions[0]);

        StudentAttendanceDetailDto dto = new StudentAttendanceDetailDto();
        dto.setStudentId(sId);
        dto.setFullName(fullName);
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setPhone(phone);

        dto.setClassId(classId);
        dto.setClassCode(classCode);
        dto.setClassName(className);

        dto.setTotalSessions(totalSessions[0]);
        dto.setPresentSessions(present[0]);
        dto.setLateSessions(late[0]);
        dto.setAbsentSessions(absent[0]);
        dto.setAttendanceRate(rate);

        dto.setHistory(history);
        return dto;
    }

    // ===================== 5) Cập nhật trạng thái 1 bản ghi =====================

    @Transactional
    public void updateAttendanceStatus(int accountId, int attendanceId, String newStatusCode) {
        // map statusCode -> tiếng Việt trong DB
        String statusVi = switch (newStatusCode) {
            case "present" -> "Có mặt";
            case "late"    -> "Muộn";
            case "absent"  -> "Vắng";
            default -> throw new IllegalArgumentException("Trạng thái không hợp lệ: " + newStatusCode);
        };

        attendanceRepository.updateAttendanceStatus(attendanceId, statusVi);
    }

    // ===================== Helpers =====================

    private static String mapStatusViToCode(String statusVi) {
        if (statusVi == null) return "absent";
        statusVi = statusVi.trim();
        if (statusVi.equalsIgnoreCase("Có mặt")) return "present";
        if (statusVi.equalsIgnoreCase("Muộn")) return "late";
        if (statusVi.equalsIgnoreCase("Vắng")) return "absent";
        return "absent";
    }

    private static Integer asInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        try { return Integer.parseInt(o.toString()); } catch (Exception e) { return null; }
    }


}
