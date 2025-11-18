package com.FinalProject.backend.Service;

import com.FinalProject.backend.Dto.TeacherDashboardStatsDto;
import com.FinalProject.backend.Repository.ClassRepository;
import com.FinalProject.backend.Repository.TeacherRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherDashboardService {

    private final TeacherRepository teacherRepository;
    private final ClassRepository classRepository;

    public TeacherDashboardService(TeacherRepository teacherRepository,
                                   ClassRepository classRepository) {
        this.teacherRepository = teacherRepository;
        this.classRepository = classRepository;
    }

    public TeacherDashboardStatsDto getStatsForAccount(int accountId) {
        Integer teacherId = teacherRepository.findTeacherIdByAccountId(accountId);
        if (teacherId == null) {
            return null;
        }

        Integer totalStudents = classRepository.countStudentsForTeacher(teacherId);
        Integer activeClasses = classRepository.countActiveClassesForTeacher(teacherId);
        Double avgScore = classRepository.averageScoreForTeacher(teacherId);
        Double attendanceRate = classRepository.attendanceRateForTeacher(teacherId);

        TeacherDashboardStatsDto dto = new TeacherDashboardStatsDto();
        dto.setTotalStudents(totalStudents != null ? totalStudents : 0);
        dto.setActiveClasses(activeClasses != null ? activeClasses : 0);
        dto.setAverageScore(avgScore != null ? avgScore : 0.0);
        dto.setAttendanceRate(attendanceRate != null ? attendanceRate : 0.0);
        return dto;
    }

    // ===== Helpers =====
    private static String asStr(Object o) {
        return o == null ? null : o.toString();
    }

    private static Integer asInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        try { return Integer.parseInt(o.toString()); } catch (Exception e) { return null; }
    }

    private static Double asDouble(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(o.toString()); } catch (Exception e) { return null; }
    }
}
