package com.FinalProject.backend.Service;

import com.FinalProject.backend.Dto.ClassScheduleDto;
import com.FinalProject.backend.Dto.TeacherScheduleDto;
import com.FinalProject.backend.Dto.StudentScheduleDto;
import com.FinalProject.backend.Models.ClassSchedule;
import com.FinalProject.backend.Models.Clazz;
import com.FinalProject.backend.Repository.ClassRepository;
import com.FinalProject.backend.Repository.ClassScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassScheduleService {

    private final ClassScheduleRepository classScheduleRepository;
    private final AccountService accountService;   // backend AccountService (lấy teacherId / studentId)
    private final ClassRepository classRepository; // để lấy entity Clazz

    /* =====================================================================
       1) API CHO GIẢNG VIÊN – LỊCH DẠY THEO THÁNG
       ===================================================================== */
    public List<TeacherScheduleDto> getSchedulesForCurrentTeacher(int year, int month) {
        // Lấy teacherId từ tài khoản đăng nhập hiện tại
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Integer teacherId = accountService.getTeacherIdByUsername(username);

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        var rows = classScheduleRepository.findScheduleOfTeacherBetween(teacherId, start, end);

        return rows.stream().map(r -> {
            TeacherScheduleDto dto = new TeacherScheduleDto();
            dto.setScheduleId(((Number) r[0]).intValue());
            dto.setClassId(((Number) r[1]).intValue());
            dto.setClassCode((String) r[2]);
            dto.setClassName((String) r[3]);
            dto.setScheduleDate(((java.sql.Date) r[4]).toLocalDate());
            dto.setStartTime(((java.sql.Time) r[5]).toLocalTime());
            dto.setEndTime(((java.sql.Time) r[6]).toLocalTime());
            dto.setRoom((String) r[7]);
            dto.setIsActive(r[8] != null ? ((Boolean) r[8]) : Boolean.TRUE);
            dto.setStudentCount(r[9] == null ? 0L : ((Number) r[9]).longValue());
            return dto;
        }).toList();
    }

    /* =====================================================================
      1b) API CHO SINH VIÊN – LỊCH HỌC THEO THÁNG
      ===================================================================== */
    @Transactional(readOnly = true)
    public List<StudentScheduleDto> getSchedulesForCurrentStudent(int year, int month) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            // tuỳ bạn muốn 401 hay 403
            throw new RuntimeException("Không xác định được thông tin tài khoản hiện tại");
        }

        String username = auth.getName();
        Integer studentId = accountService.getStudentIdByUsername(username);

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        var rows = classScheduleRepository.findScheduleOfStudentBetween(studentId, start, end);

        return rows.stream().map(r -> {
            StudentScheduleDto dto = new StudentScheduleDto();
            dto.setScheduleId(((Number) r[0]).intValue());
            dto.setClassId(((Number) r[1]).intValue());
            dto.setClassCode((String) r[2]);
            dto.setClassName((String) r[3]);
            dto.setDate(((java.sql.Date) r[4]).toLocalDate());
            dto.setStartTime(((java.sql.Time) r[5]).toLocalTime());
            dto.setEndTime(((java.sql.Time) r[6]).toLocalTime());
            dto.setRoom((String) r[7]);
            dto.setIsActive(r[8] != null ? ((Boolean) r[8]) : Boolean.TRUE);
            dto.setStudentCount(r[9] == null ? 0L : ((Number) r[9]).longValue());
            return dto;
        }).toList();
    }

    /* =====================================================================
       2) API CHO ADMIN – QUẢN LÝ LỊCH HỌC THEO LỚP
       ===================================================================== */

    /**
     * Lấy tất cả lịch học của 1 lớp (dùng cho modal Lịch học ở Admin)
     * chỉ những bản ghi IsDeleted = 0
     */
    @Transactional(readOnly = true)
    public List<ClassScheduleDto> getSchedulesByClassId(Integer classId) {
        List<ClassSchedule> list = classScheduleRepository
                .findByClazz_ClassIdAndIsDeletedFalse(classId);

        return list.stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Tạo mới một lịch học
     */
    @Transactional
    public void createSchedule(ClassScheduleDto dto) {
        if (dto.getClassId() == null) {
            throw new IllegalArgumentException("ClassId không được null");
        }

        Clazz clazz = classRepository.findById(dto.getClassId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy lớp với id = " + dto.getClassId()));
        if (Boolean.TRUE.equals(clazz.getStatus())) {
            throw new IllegalStateException("Lớp đã hoàn thành, không thể thêm lịch mới");
        }
        ClassSchedule cs = new ClassSchedule();
        cs.setClazz(clazz);
        cs.setScheduleDate(dto.getScheduleDate());
        cs.setStartTime(dto.getStartTime());
        cs.setEndTime(dto.getEndTime());
        cs.setRoom(dto.getRoom());
        cs.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : Boolean.TRUE);
        cs.setIsDeleted(false); // mặc định chưa bị xóa

        classScheduleRepository.save(cs);
    }

    /**
     * Cập nhật thông tin một lịch học
     */
    @Transactional
    public void updateSchedule(Integer scheduleId, ClassScheduleDto dto) {
        ClassSchedule cs = classScheduleRepository.findByScheduleIdAndIsDeletedFalse(scheduleId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy lịch học với id = " + scheduleId));

        Clazz clazz = cs.getClazz();
        if (clazz != null && Boolean.TRUE.equals(clazz.getStatus())) {
            throw new IllegalStateException("Lớp đã hoàn thành, không thể chỉnh sửa lịch học");
        }

        // phần còn lại giữ nguyên
        if (dto.getScheduleDate() != null) cs.setScheduleDate(dto.getScheduleDate());
        if (dto.getStartTime() != null) cs.setStartTime(dto.getStartTime());
        if (dto.getEndTime() != null) cs.setEndTime(dto.getEndTime());
        cs.setRoom(dto.getRoom());
        if (dto.getIsActive() != null) cs.setIsActive(dto.getIsActive());

        classScheduleRepository.save(cs);
    }

    /**
     * Bật / tắt IsActive (tạm hoãn) – dùng cho checkbox
     */
    @Transactional
    public void updateActive(Integer scheduleId, Boolean isActive) {
        ClassSchedule cs = classScheduleRepository.findByScheduleIdAndIsDeletedFalse(scheduleId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy lịch học với id = " + scheduleId));

        cs.setIsActive(isActive != null ? isActive : Boolean.TRUE);
        classScheduleRepository.save(cs);
    }

    /**
     * Soft delete: set IsDeleted = 1
     */
    @Transactional
    public void deleteSchedule(Integer scheduleId) {
        // Nếu muốn check tồn tại:
        // classScheduleRepository.findByScheduleIdAndIsDeletedFalse(scheduleId)
        //        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch học với id = " + scheduleId));
        classScheduleRepository.softDelete(scheduleId);
    }

    /* =====================================================================
       Helper: mapping Entity <-> DTO
       ===================================================================== */

    private ClassScheduleDto toDto(ClassSchedule cs) {
        ClassScheduleDto dto = new ClassScheduleDto();
        dto.setScheduleId(cs.getScheduleId());
        dto.setClassId(cs.getClazz() != null ? cs.getClazz().getClassId() : null);
        dto.setScheduleDate(cs.getScheduleDate());
        dto.setStartTime(cs.getStartTime());
        dto.setEndTime(cs.getEndTime());
        dto.setRoom(cs.getRoom());
        dto.setIsActive(cs.getIsActive());
        // dto không cần IsDeleted, FE không dùng
        return dto;
    }

}
