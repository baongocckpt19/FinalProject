// src/main/java/com/FinalProject/backend/Service/StudentDashboardService.java
package com.FinalProject.backend.Service;

import com.FinalProject.backend.Dto.AttendanceSummaryDto;
import com.FinalProject.backend.Dto.GradeByClassDto;
import com.FinalProject.backend.Dto.StudentDashboardDto;
import com.FinalProject.backend.Models.Student;
import com.FinalProject.backend.Repository.AttendanceRepository;
import com.FinalProject.backend.Repository.GradeRepository;
import com.FinalProject.backend.Repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


// Service xử lý logic lấy dữ liệu Dashboard cho sinh viên.

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentDashboardService {

    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;

    /**
     * Lấy dashboard cho 1 account (tài khoản đang đăng nhập).
     *
     * @param accountId id của Account (AccountId).
     */
    public StudentDashboardDto getDashboardForAccount(Integer accountId) {
        // 1) Tìm Student tương ứng account
        Student student = studentRepository.findByAccountId(accountId);
        if (student == null) {
            // Nếu tài khoản không phải sinh viên hoặc chưa map Student -> có thể trả về null hoặc throw exception.
            return null;
        }

        Integer studentId = student.getStudentId();
        return getDashboardForStudent(studentId);
    }

    /**
     * Lấy dashboard cho 1 sinh viên cụ thể.
     *  - Hàm này tách riêng để sau này có thể tái sử dụng (nếu cần).
     */
    public StudentDashboardDto getDashboardForStudent(Integer studentId) {
        StudentDashboardDto dto = new StudentDashboardDto();

        // ===================== 1) ĐIỂM + GPA + TỈ LỆ QUA MÔN =====================

        // Điểm trung bình thang 10, đã áp dụng 25% CC, 25% GK, 50% CK
        Double avg10 = gradeRepository.calcAverageGpa10(studentId);
        if (avg10 == null) avg10 = 0.0;

           // GPA hiển thị là chính avg10 luôn (thang 10)
        double gpa = avg10;


        Long passed = gradeRepository.countPassedSubjects(studentId);
        Long total = gradeRepository.countAllSubjects(studentId);
        if (passed == null) passed = 0L;
        if (total == null) total = 0L;

        double passRate = 0.0;
        if (total > 0) {
            passRate = passed * 100.0 / total;
        }

        // Lấy danh sách tối đa 6 lớp
        List<Object[]> rows = gradeRepository.findTop6GradesByStudent(studentId);
        List<GradeByClassDto> gradeList = new ArrayList<>();

        for (Object[] row : rows) {
            GradeByClassDto g = new GradeByClassDto();
            g.setClassId((Integer) row[0]);            // ClassId
            g.setClassCode((String) row[1]);           // ClassCode
            g.setClassName((String) row[2]);           // ClassName

            if (row[3] != null) {
                g.setFinalGrade(((Number) row[3]).doubleValue()); // Average10
            } else {
                g.setFinalGrade(null);
            }

            gradeList.add(g);
        }


        // ===================== 2) TÓM TẮT ĐIỂM DANH =====================

        // Lấy danh sách kết quả (dù query chỉ trả 1 hàng, Spring Data vẫn wrap dạng List<Object[]>)
        List<Object[]> attList = attendanceRepository.summarizeAttendanceByStudent(studentId);

        long present = 0, absent = 0, late = 0;

        // Lấy hàng đầu tiên nếu có
        if (attList != null && !attList.isEmpty()) {
            Object[] attRow = attList.get(0);

            // Chú ý: từng phần tử attRow[i] là Number (Long/BigDecimal...) chứ không phải Object[]
            present = attRow[0] != null ? ((Number) attRow[0]).longValue() : 0L;
            absent  = attRow[1] != null ? ((Number) attRow[1]).longValue() : 0L;
            late    = attRow[2] != null ? ((Number) attRow[2]).longValue() : 0L;
        }

        long totalSessions = present + absent + late;
        double attendanceRate = 0.0;
        if (totalSessions > 0) {
            attendanceRate = present * 100.0 / totalSessions;
        }

        AttendanceSummaryDto summary = new AttendanceSummaryDto();
        summary.setPresentCount(present);
        summary.setAbsentCount(absent);
        summary.setLateCount(late);


        // ===================== 3) GÁN VÀO DTO TRẢ VỀ =====================

        dto.setGpa(round(gpa, 2));
        dto.setPassRate(round(passRate, 1));
        dto.setAbsentCount((int) absent);
        dto.setAttendanceRate(round(attendanceRate, 1));
        dto.setGrades(gradeList);
        dto.setAttendanceSummary(summary);

        return dto;
    }

    /**
     * Hàm làm tròn số với scale chữ số sau dấu phẩy.
     *  - Ví dụ round(3.14159, 2) -> 3.14
     */
    private double round(double value, int scale) {
        if (Double.isNaN(value)) return 0.0;
        double factor = Math.pow(10, scale);
        return Math.round(value * factor) / factor;
    }


}
