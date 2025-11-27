package com.FinalProject.backend.Service;

import com.FinalProject.backend.Dto.ClassAttendanceStudentDto;
import com.FinalProject.backend.Dto.StudentAttendanceHistoryDto;
import com.FinalProject.backend.Models.Attendance;
import com.FinalProject.backend.Models.ClassSchedule;
import com.FinalProject.backend.Models.Clazz;
import com.FinalProject.backend.Repository.AttendanceRepository;
import com.FinalProject.backend.Repository.ClassScheduleRepository;
import com.FinalProject.backend.Repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherAttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final TeacherRepository teacherRepository;   // ⭐ thêm vào

    // ====== 1) Lấy chi tiết điểm danh lớp theo 1 buổi (JSON cho FE) ======
    public List<ClassAttendanceStudentDto> getClassAttendanceBySchedule(Integer scheduleId) {
        // (đã bỏ check quyền như bạn muốn)

        List<Object[]> rows = attendanceRepository.findClassAttendanceBySchedule(scheduleId);

        return rows.stream().map(r -> {
            int i = 0;
            ClassAttendanceStudentDto dto = new ClassAttendanceStudentDto();
            dto.setClassId(((Number) r[i++]).intValue());          // 0
            dto.setScheduleId(((Number) r[i++]).intValue());       // 1
            dto.setStudentId(((Number) r[i++]).intValue());        // 2
            dto.setFullName((String) r[i++]);                      // 3
            dto.setUsername((String) r[i++]);                      // 4

            // ⭐ map email / phone
            dto.setEmail((String) r[i++]);                         // 5
            dto.setPhone((String) r[i++]);                         // 6

            Object attIdObj = r[i++];                              // 7
            dto.setAttendanceId(attIdObj != null
                    ? ((Number) attIdObj).intValue()
                    : null);

            String statusVi = (String) r[i++];                     // 8
            dto.setStatus(mapStatusViToCode(statusVi));            // present/absent/late/none

            Time t = (Time) r[i++];                                // 9
            dto.setAttendanceTime(t != null ? t.toString() : null);

            return dto;
        }).toList();
    }

    // ====== 2) Cập nhật / tạo mới điểm danh (giữ nguyên như bạn đang dùng) ======
    public void upsertAttendance(Integer scheduleId,
                                 Integer studentId,
                                 String statusCode) {
        ClassSchedule schedule = classScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy buổi học"));

        Clazz clazz = schedule.getClazz();
        if (clazz == null) {
            throw new IllegalArgumentException("Buổi học không gắn với lớp nào");
        }

        String statusVi = mapStatusCodeToVi(statusCode);

        Attendance att = attendanceRepository.findByStudentIdAndScheduleId(studentId, scheduleId);
        if (att == null) {
            att = new Attendance();
            att.setStudentId(studentId);
            att.setClassId(clazz.getClassId());
            att.setScheduleId(scheduleId);
        }

        att.setStatus(statusVi);
        att.setAttendanceTime(LocalTime.now());

        attendanceRepository.save(att);
    }

    // ====== 3) Xuất CSV (logic nằm ở service) ======

    @Transactional(readOnly = true)
    public byte[] exportAttendanceCsv(Integer scheduleId) {
        // 3.1 Lấy thông tin schedule + lớp + giảng viên
        ClassSchedule schedule = classScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy buổi học"));

        Clazz clazz = schedule.getClazz();   // Lúc này vẫn còn session, LAZY load OK
        String className  = clazz != null ? clazz.getClassName()  : "";
        String classCode  = clazz != null ? clazz.getClassCode()  : "";
        Integer teacherId = clazz != null ? clazz.getTeacherId()  : null;

        String teacherName = "";
        if (teacherId != null) {
            Object t = teacherRepository.findTeacherShortById(teacherId);
            if (t != null) {
                Object[] arr = (Object[]) t;
                teacherName = (String) arr[1]; // TeacherId, FullName
            }
        }

        String ngayDay   = schedule.getScheduleDate() != null ? schedule.getScheduleDate().toString() : "";
        String timeRange = "";
        if (schedule.getStartTime() != null && schedule.getEndTime() != null) {
            timeRange = schedule.getStartTime() + " - " + schedule.getEndTime();
        }
        String room = schedule.getRoom() != null ? schedule.getRoom() : "";

        // 3.2 Lấy danh sách sinh viên & điểm danh
        List<ClassAttendanceStudentDto> rows = getClassAttendanceBySchedule(scheduleId);

        StringBuilder sb = new StringBuilder();

        // ⭐ Thêm BOM để Excel nhận đúng UTF-8, fix CÃ³ máº·t
        sb.append('\uFEFF');

        // Header thông tin lớp
        sb.append("Lớp,").append(escapeCsv(className))
                .append(" (").append(classCode).append(")").append('\n');
        sb.append("Giảng viên,").append(escapeCsv(teacherName)).append('\n');
        sb.append("Ngày dạy,").append(ngayDay).append('\n');
        sb.append("Thời gian,").append(timeRange).append('\n');
        sb.append("Phòng,").append(escapeCsv(room)).append('\n');

        sb.append('\n'); // dòng trống

        // Header bảng sinh viên
        sb.append("STT,Tên,Mã số sinh viên,Thời gian điểm danh,Trạng thái điểm danh\n");

        int index = 1;
        for (ClassAttendanceStudentDto r : rows) {
            String statusText = mapStatusCodeToVi(r.getStatus());  // present/absent/late -> Có mặt/Vắng/Muộn
            String time = r.getAttendanceTime() != null ? r.getAttendanceTime() : "";

            sb.append(index++).append(',')
                    .append(escapeCsv(r.getFullName())).append(',')
                    // Mã số sinh viên = StudentId theo yêu cầu
                    .append(r.getStudentId() != null ? r.getStudentId() : 0).append(',')
                    .append(time).append(',')
                    .append(statusText)
                    .append('\n');
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ====== Helper: map tiếng Việt <-> code ======
    private static String mapStatusViToCode(String statusVi) {
        if (statusVi == null) return "none";
        statusVi = statusVi.trim();
        if (statusVi.equalsIgnoreCase("Có mặt")) return "present";
        if (statusVi.equalsIgnoreCase("Muộn"))   return "late";
        if (statusVi.equalsIgnoreCase("Vắng"))   return "absent";
        return "none";
    }

    private static String mapStatusCodeToVi(String code) {
        if (code == null) return "Vắng";
        return switch (code) {
            case "present" -> "Có mặt";
            case "late"    -> "Muộn";
            case "absent"  -> "Vắng";
            default        -> "Vắng";
        };
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        String v = value.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v + "\"";
        }
        return v;
    }


    // ====== 3b) Lịch sử điểm danh của 1 sinh viên trong 1 lớp ======
    @Transactional(readOnly = true)
    public List<StudentAttendanceHistoryDto> getStudentHistory(Integer classId, Integer studentId) {
        List<Object[]> rows = attendanceRepository.findStudentHistoryByClass(classId, studentId);

        return rows.stream().map(r -> {
            int i = 0;
            Integer scheduleId = ((Number) r[i++]).intValue();     // 0
            Date dateSql       = (Date) r[i++];                    // 1
            Time startSql      = (Time) r[i++];                    // 2
            Time endSql        = (Time) r[i++];                    // 3
            String statusVi    = (String) r[i++];                  // 4
            Time attTimeSql    = (Time) r[i++];                    // 5

            LocalDate date = dateSql != null ? dateSql.toLocalDate() : null;
            LocalTime start = startSql != null ? startSql.toLocalTime() : null;
            LocalTime end = endSql != null ? endSql.toLocalTime() : null;

            String statusCode = mapStatusViToCode(statusVi);       // present/absent/late/none
            String attendanceTime = attTimeSql != null ? attTimeSql.toString() : null;

            StudentAttendanceHistoryDto dto = new StudentAttendanceHistoryDto();
            dto.setScheduleId(scheduleId);
            dto.setDate(date);
            dto.setStartTime(start);
            dto.setEndTime(end);
            dto.setStatus(statusCode);
            dto.setAttendanceTime(attendanceTime);

            return dto;
        }).toList();
    }
}
