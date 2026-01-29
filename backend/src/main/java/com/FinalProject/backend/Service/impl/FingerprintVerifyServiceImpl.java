//đây là FingerprintVerifyServiceImpl.java
package com.FinalProject.backend.Service.impl;

import com.FinalProject.backend.Dto.FingerprintVerifyRequest;
import com.FinalProject.backend.Dto.FingerprintVerifyResponse;
import com.FinalProject.backend.Models.Attendance;
import com.FinalProject.backend.Models.ClassSchedule;
import com.FinalProject.backend.Models.Student;
import com.FinalProject.backend.Models.Device;
import com.FinalProject.backend.Models.DeviceFingerprintSlot;
import com.FinalProject.backend.Repository.AttendanceRepository;
import com.FinalProject.backend.Repository.ClassScheduleRepository;
import com.FinalProject.backend.Repository.DeviceFingerprintSlotRepository;
import com.FinalProject.backend.Repository.DeviceRepository;
import com.FinalProject.backend.Repository.StudentRepository;
import com.FinalProject.backend.Service.FingerprintVerifyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FingerprintVerifyServiceImpl implements FingerprintVerifyService {

    private final DeviceRepository deviceRepository;
    private final DeviceFingerprintSlotRepository deviceFingerprintSlotRepository;
    private final StudentRepository studentRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final AttendanceRepository attendanceRepository;

    // mở trước giờ học 30 phút
    private static final int PRE_OPEN_MINUTES = 30;

    // 15 phút đầu coi là "Có mặt", sau đó là "Muộn"
    private static final int LATE_THRESHOLD_MINUTES = 15;

    @Override
    @Transactional
    public FingerprintVerifyResponse verify(FingerprintVerifyRequest request) {
        FingerprintVerifyResponse resp = new FingerprintVerifyResponse();

        if (request.getDeviceCode() == null || request.getSensorSlot() == null) {
            resp.setSuccess(false);
            resp.setStatus("fail");
            resp.setMessage("Thiếu deviceCode hoặc sensorSlot");
            return resp;
        }

        // 1. Xác định thời điểm hiện tại (hoặc theo timestamp ESP gửi)
        LocalDateTime now = parseTimestampOrNow(request.getTimestamp());

        // 2. Tìm device theo deviceCode
        Device device = deviceRepository.findByDeviceCode(request.getDeviceCode())
                .orElse(null);
        if (device == null || Boolean.FALSE.equals(device.getIsActive())) {
            resp.setSuccess(false);
            resp.setStatus("fail");
            resp.setMessage("Thiết bị không tồn tại hoặc đã bị khóa");
            return resp;
        }

        // 3. Tìm mapping (DeviceId + SensorSlot) -> StudentId
        Integer deviceId = device.getDeviceId();

        DeviceFingerprintSlot slot = deviceFingerprintSlotRepository
                .findByIdDeviceIdAndSensorSlot(deviceId, request.getSensorSlot())
                .orElse(null);

        if (slot == null || slot.getId() == null || slot.getId().getStudentId() == null) {
            resp.setSuccess(false);
            resp.setStatus("fail");
            resp.setMessage("Không tìm thấy sinh viên gắn với vân tay này");
            return resp;
        }

        Integer studentId = slot.getId().getStudentId();
        resp.setStudentId(studentId);

        Student student = studentRepository.findById(studentId).orElse(null);
        if (student != null) {
            resp.setFullName(student.getFullName());
        }

        // 4. Tìm ClassSchedule phù hợp theo Room + ngày + window 30'
        String room = device.getRoom();          // vd: "Phòng Lab 1"
        LocalDate today = now.toLocalDate();

        List<ClassSchedule> schedules = classScheduleRepository
                .findByRoomAndScheduleDateAndIsActiveTrueAndIsDeletedFalse(room, today);

        ClassSchedule matched = schedules.stream()
                .filter(cs -> isInCheckinWindow(now, cs))
                .min(Comparator.comparing(ClassSchedule::getStartTime)) // nếu chồng chéo, lấy buổi sớm nhất
                .orElse(null);

        if (matched == null) {
            resp.setSuccess(false);
            resp.setStatus("fail");
            resp.setMessage("Không có tiết học nào trong thời gian này.");
            return resp;
        }

        Integer classId = matched.getClazz().getClassId();
        Integer scheduleId = matched.getScheduleId();
        resp.setClassId(classId);
        resp.setScheduleId(scheduleId);

        // 5. Kiểm tra đã có Attendance chưa
        Attendance existing = attendanceRepository
                .findByStudentIdAndScheduleId(studentId, scheduleId);

        if (existing != null) {
            resp.setSuccess(true);
            resp.setStatus("duplicate");
            resp.setMessage("Sinh viên đã điểm danh trước đó.");
            resp.setAttendanceTime(existing.getAttendanceTime().toString());
            return resp;
        }

        // 6. Tính PRESENT / LATE dựa trên mốc 15'
        LocalDateTime startDateTime = LocalDateTime.of(
                matched.getScheduleDate(),
                matched.getStartTime()
        );
        LocalDateTime lateBoundary = startDateTime.plusMinutes(LATE_THRESHOLD_MINUTES);

        String statusVi;
        String statusCode;

        if (!now.isAfter(lateBoundary)) {
            statusVi = "Có mặt";
            statusCode = "present";
        } else {
            statusVi = "Muộn";
            statusCode = "late";
        }

        // 7. Lưu Attendance mới
        Attendance att = new Attendance();
        att.setStudentId(studentId);
        att.setClassId(classId);
        att.setScheduleId(scheduleId);
        att.setAttendanceTime(now.toLocalTime());
        att.setStatus(statusVi);

        attendanceRepository.save(att);

        resp.setSuccess(true);
        resp.setStatus(statusCode);
        resp.setMessage("Điểm danh thành công");
        resp.setAttendanceTime(att.getAttendanceTime().toString());

        return resp;
    }

    private LocalDateTime parseTimestampOrNow(String ts) {
        if (ts == null || ts.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(ts); // ISO-8601
        } catch (DateTimeParseException ex) {
            return LocalDateTime.now();
        }
    }

    private boolean isInCheckinWindow(LocalDateTime now, ClassSchedule cs) {
        LocalDateTime start = LocalDateTime.of(cs.getScheduleDate(), cs.getStartTime());
        LocalDateTime end = LocalDateTime.of(cs.getScheduleDate(), cs.getEndTime());
        LocalDateTime open = start.minusMinutes(PRE_OPEN_MINUTES);

        return !now.isBefore(open) && !now.isAfter(end);
    }
}
